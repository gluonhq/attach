/*
 * Copyright (c) 2020, 2026, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.helloandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Entry point for the pictures service Android implementation.
 * It verifies permissions, stages capture files, preprocesses images,
 * and bridges camera results back to the native Attach layer.
 */
public class DalvikPicturesService  {

    private static final String TAG = Util.TAG;
    private static final int SELECT_PICTURE = 1;

    private final Activity activity;
    private final boolean debug;
    private boolean verified;

    private String photoPath;

    private CameraController cameraController;

    public DalvikPicturesService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
        clearCache();
    }

    private CameraController getOrCreateCameraController() {
        if (cameraController == null) {
            cameraController = new CameraController(activity, TAG, debug, new CameraController.Listener() {
                @Override
                public void onCaptured(File targetFile, boolean savePhoto) {
                    handleCapturedPhoto(targetFile, savePhoto);
                }

                @Override
                public void onCancelled() {
                    sendCancelled();
                }
            });
        }
        return cameraController;
    }

    private boolean verifyPermissions() {
        if (!verified) {
            if (Build.VERSION.SDK_INT >= 33) {
                verified = Util.verifyPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO);
            } else {
                verified = Util.verifyPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return verified;
    }

    public void takePhoto(final boolean savePhoto) {
        if (!verifyPermissions()) {
            Log.v(TAG, "Permission verification failed: Camera disabled");
            return;
        }
        clearCache();
        File photoFile = createCaptureFile(savePhoto);
        if (photoFile == null) {
            sendCancelled();
            return;
        }
        photoPath = "file:" + photoFile.getAbsolutePath();
        if (debug) {
            Log.v(TAG, "Camera capture requested. Picture file: " + photoPath);
        }
        if (!getOrCreateCameraController().start(savePhoto, photoFile)) {
            Log.e(TAG, "Camera startup failed");
            sendCancelled();
        }
    }

    private File createCaptureFile(boolean savePhoto) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        try {
            File photo = savePhoto
                    ? new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IMG_" + timeStamp + ".jpg")
                    : File.createTempFile("IMG_" + timeStamp, ".jpg", activity.getCacheDir());
            if (photo.exists()) {
                photo.delete();
            } else if (photo.getParentFile() != null) {
                photo.getParentFile().mkdirs();
            }
            return photo;
        } catch (IOException e) {
            Log.e(TAG, "Error creating file " + e.getMessage());
            return null;
        }
    }

    private void handleCapturedPhoto(File photoFile, boolean savePhoto) {
        if (photoFile == null || !photoFile.exists()) {
            Log.e(TAG, "Picture file doesn't exist for: " + (photoFile == null ? "null" : photoFile.getAbsolutePath()));
            return;
        }
        Uri imageUri = Uri.fromFile(photoFile);
        int imageRotation = getImageRotation(imageUri);
        if (debug) {
            Log.v(TAG, "Image file located at " + photoFile.getAbsolutePath() + " with rotation: " + imageRotation);
        }

        String originalPath = photoFile.getAbsolutePath();
        if (savePhoto) {
            MediaScannerConnection.scanFile(activity, new String[]{photoFile.toString()}, null, null);
            photoFile = copyToCache(photoFile);
        }
        ImagePreprocessor.preprocessImage(photoFile, imageRotation, debug, TAG);
        sendPhotoFile(originalPath, photoFile.getAbsolutePath());
    }

    private void selectPicture() {
        clearCache();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");

        if (activity == null) {
            Log.e(TAG, "Activity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        IntentHandler intentHandler = new IntentHandler() {
            @Override
            public void gotActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
                    if (debug) {
                        Log.v(TAG, "Picture successfully selected");
                    }
                    Uri selectedImageUri = intent.getData();
                    if (selectedImageUri != null) {
                        if (debug) {
                            Log.v(TAG, "Copy image file");
                        }
                        File cachePhotoFile = copyFile(selectedImageUri);
                        if (debug) {
                            Log.v(TAG, "Setting image file: " + cachePhotoFile.getAbsolutePath());
                        }
                        if (cachePhotoFile.exists()) {
                            int imageRotation = getImageRotation(selectedImageUri);
                            if (debug) {
                                Log.v(TAG, "Image file located at " + cachePhotoFile.getAbsolutePath() + " with rotation: " + imageRotation);
                            }
                            String originalPath = cachePhotoFile.getAbsolutePath();
                            ImagePreprocessor.preprocessImage(cachePhotoFile, imageRotation, debug, TAG);
                            sendPhotoFile(originalPath, cachePhotoFile.getAbsolutePath());
                        }
                    }
                } else if (requestCode == SELECT_PICTURE) {
                    sendCancelled();
                }
            }
        };

        Util.setOnActivityResultHandler(intentHandler);

        // check for permissions
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);
        } else {
            Log.e(TAG, "GalleryActivity: resolveActivity failed");
        }
    }

    private void clearCache() {
        File[] files = activity.getCacheDir().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.exists() && file.getName().endsWith(".jpg")) {
                file.delete();
            }
        }
    }

    private String getImageName(Uri uri) {
        try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return "image.jpg";
    }

    private int getImageRotation(Uri uri) {
        try {
            ExifInterface ei;
            if (Build.VERSION.SDK_INT > 23) {
                try (InputStream is = activity.getContentResolver().openInputStream(uri)) {
                    ei = new ExifInterface(is);
                }
            } else {
                ei = new ExifInterface(uri.getPath());
            }

            int orientation =  ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, "getImageRotation failed "+ e.getMessage());
        }
        return 0;
    }

    private File copyFile(Uri uri) {
        File selectedFile = new File(activity.getCacheDir(), getImageName(uri));
        try (InputStream is = activity.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(selectedFile)) {
            byte[] buffer = new byte[32 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException ex) {
            Log.e(TAG, null, ex);
        } catch (IOException ex) {
            Log.e(TAG, null, ex);
        }
        return selectedFile;
    }

    private File copyToCache(File source) {
        File dest = new File(activity.getCacheDir(), "display_" + source.getName());
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[32 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            Log.e(TAG, "copyToCache failed: " + ex.getMessage());
            return source; // fall back to original
        }
        return dest;
    }

    // native
    private native void sendPhotoFile(String originalFilePath, String processedFilePath);
    private native void sendCancelled();

}

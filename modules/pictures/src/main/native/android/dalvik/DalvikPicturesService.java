/*
 * Copyright (c) 2020, Gluon
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
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class DalvikPicturesService  {

    private static final String TAG = Util.TAG;
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;

    private final Activity activity;
    private final boolean debug;
    private boolean verified;

    private final String authority;
    private String photoPath;

    public DalvikPicturesService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
        authority = activity.getPackageName() + ".fileprovider";
        clearCache();
    }

    private boolean verifyPermissions() {
        if (!verified) {
            verified = Util.verifyPermissions(Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return verified;
    }

    public void takePhoto(final boolean savePhoto) {
        if (!verifyPermissions()) {
            Log.v(TAG, "Permission verification failed: Camera disabled");
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Create the file where the photo should go
        try {
            File photo = savePhoto ? new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "IMG_"+ timeStamp + ".jpg") :
                    File.createTempFile("IMG_"+ timeStamp, ".jpg", activity.getCacheDir());
            if (photo.exists()) {
                photo.delete();
            } else {
                photo.getParentFile().mkdirs();
            }
            photoPath = "file:" + photo.getAbsolutePath();
            if (debug) {
                Log.v(TAG, "Picture file: " + photoPath);
            }

            Uri photoUri = FileProvider.getUriForFile(activity, authority, photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (IOException e) {
            Log.e(TAG, "Error creating file " + e.getMessage());
        }

        IntentHandler intentHandler = new IntentHandler() {
            @Override
            public void gotActivityResult (int requestCode, int resultCode, Intent intent) {
                if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
                    if (debug) {
                        Log.v(TAG, "Picture successfully taken at " + photoPath);
                    }
                    Uri imageUri = Uri.parse(photoPath);
                    File photoFile = new File(imageUri.getPath());
                    if (photoFile.exists()) {
                        int imageRotation = getImageRotation(imageUri);
                        if (debug) {
                            Log.v(TAG, "Image file located at " + photoFile.getAbsolutePath() + " with rotation: " + imageRotation);
                        }
                        sendPhotoFile(photoFile.getAbsolutePath(), imageRotation);

                        if (savePhoto) {
                            // media scanner to rescan DIRECTORY_PICTURES after an image is saved/deleted
                            MediaScannerConnection.scanFile(activity, new String[]{photoFile.toString()}, null, null);
                        }
                    } else {
                        Log.e(TAG, "Picture file doesn't exist for: " + photoFile.getAbsolutePath());
                    }
                }
            }
        };

        if (activity == null) {
            Log.e(TAG, "Activity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        Util.setOnActivityResultHandler(intentHandler);

        // check for permissions
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, TAKE_PICTURE);
        } else {
            Log.e(TAG, "GalleryActivity: resolveActivity failed");
        }
    }

    private void selectPicture() {
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
                            sendPhotoFile(cachePhotoFile.getAbsolutePath(), imageRotation);
                        }
                    }
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
                ei = new ExifInterface(activity.getContentResolver().openInputStream(uri));
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
            byte[] buffer = new byte[8 * 1024];
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

    // native
    private native void sendPhotoFile(String filePath, int rotate);

}

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

public class DalvikFileChooserService {

    private static final String TAG = Util.TAG;
    private static final int RESULT_OK = -1;
    private static final int SELECT_FILE = 1;

    private final Activity activity;
    private final boolean debug;
    private boolean verified;

    private final String authority;
    private String filePath;

    public DalvikFileChooserService(Activity activity) {
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

    private void selectFile() {
        Log.v(TAG, "Select File method called.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");

        if (activity == null) {
            Log.e(TAG, "Activity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        IntentHandler intentHandler = new IntentHandler() {
            @Override
            public void gotActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
                    Log.v(TAG, "File successfully selected");
                    Uri selectedFileUri = intent.getData();
                    if (selectedFileUri != null) {
                        if (debug) {
                            Log.v(TAG, "Copy file");
                        }
                        File cachePhotoFile = copyFile(selectedFileUri);
                        if (debug) {
                            Log.v(TAG, "Setting file: " + cachePhotoFile.getAbsolutePath());
                        }
                        if (cachePhotoFile.exists()) {
                            if (debug) {
                                Log.v(TAG, "File located at " + cachePhotoFile.getAbsolutePath());
                            }
                            sendFile(cachePhotoFile.getAbsolutePath(), 0);
                        }
                    }
                } else {
                    Log.v(TAG, "File not successfully selected");
                }
            }
        };

        Util.setOnActivityResultHandler(intentHandler);

        // check for permissions
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
        } else {
            Log.e(TAG, "GalleryActivity: resolveActivity failed");
        }
    }

    private void clearCache() { // Adapted for all files
        File[] files = activity.getCacheDir().listFiles();
        for (File file : files) {
            file.delete();
        }
    }

    private String getFileName(Uri uri) {
        try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return "image.jpg";
    }

    private File copyFile(Uri uri) {
        File selectedFile = new File(activity.getCacheDir(), getFileName(uri));
        try (InputStream is = activity.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(selectedFile)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException ex) {
            Log.e(TAG, "FILE NOT FOUND", ex);
        } catch (IOException ex) {
            Log.e(TAG, "IO ISSUES", ex);
        }
        return selectedFile;
    }

    // native
    private native void sendFile(String filePath, int rotate);

}

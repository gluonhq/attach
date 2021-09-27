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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.util.List;

public class DalvikOpenService {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final String authority;
    private final boolean debug;

    public DalvikOpenService(Activity activity) {
        this.activity = activity;
        authority = activity.getPackageName() + ".fileprovider";
        debug = Util.isDebug();
    }

    private void openFile(String fileName) {
        Intent openingIntent = new Intent(Intent.ACTION_VIEW);
        File file = new File(fileName);

        if (file != null && file.exists()) {
            final Uri uriFile = FileProvider.getUriForFile(activity, authority, file);
            openingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            openingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            openingIntent.setData(uriFile);
        } else {
            Log.e(TAG, "Error: A non empty file is required");
            return;
        }

        Intent chooser = Intent.createChooser(openingIntent, "Open file");
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        chooser.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        activity.startActivity(chooser);
    }

    private void openFileWithType(String type, String fileName) {
        Intent openingIntent = new Intent(Intent.ACTION_VIEW);

        if (type != null && !type.isEmpty()) {
            openingIntent.setType(type);
        } else {
            Log.e(TAG, "Error: A non empty type is required");
            return;
        }
        File file = new File(fileName);
        if (file != null && file.exists()) {
            if (debug) {
                Log.v(TAG, String.format("File to open: %s", file));
                Log.v(TAG, "Application name provider: " + authority);
            }
            final Uri uriFile = FileProvider.getUriForFile(activity, authority, file);
            if (debug) {
                Log.v(TAG, String.format("Opened file URI: %s", file));
            }
            openingIntent.putExtra(Intent.EXTRA_STREAM, uriFile);
            openingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            Log.e(TAG, "Error: A non empty file is required");
            return;
        }
        if (debug) {
            Log.v(TAG, "Start file opening intent");
        }

        Intent chooser = Intent.createChooser(openingIntent, null);
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        chooser.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivity(chooser);

    }


}

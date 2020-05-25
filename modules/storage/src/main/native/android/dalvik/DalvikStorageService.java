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
import android.os.Environment;
import android.util.Log;

public class DalvikStorageService {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final boolean debug;

    public DalvikStorageService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
    }

    /**
     * On Android, typical valid subdirectories are:
     * - Alarms
     * - DCIM
     * - Documents
     * - Download
     * - Movies
     * - Music
     * - Notifications
     * - Pictures
     * - Podcasts
     * - Ringtones
     * @param subdirectory
     * @return String with the path of the subdirectory
     */
    private String getPublicStorage(String subdirectory) {
        if (!verifyPermissions()) {
            Log.v(TAG, "External Storage permissions disabled");
            return null;
        }
        String path = Environment.getExternalStoragePublicDirectory(subdirectory).getAbsolutePath();
        if (debug) {
            Log.v(TAG, "External public storage path: " + path);
        }
        return path;
    }

    private boolean isExternalStorageWritable() {
        if (!verifyPermissions()) {
            Log.v(TAG, "Not enough permissions to write to the External Storage");
            return false;
        }
        String state = Environment.getExternalStorageState();
        boolean writable = Environment.MEDIA_MOUNTED.equals(state);
        if (debug) {
            Log.v(TAG, "External storage is writable: " + writable);
        }
        return writable;
    }

    private boolean isExternalStorageReadable() {
        if (!verifyPermissions()) {
            Log.v(TAG, "Not enough permissions to read the External Storage");
            return false;
        }
        String state = Environment.getExternalStorageState();
        boolean readable = Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        if (debug) {
            Log.v(TAG, "External public is readable: " + readable);
        }
        return readable;
    }

    private boolean verifyPermissions() {
        return Util.verifyPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
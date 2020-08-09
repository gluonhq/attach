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
import java.io.File;

public class DalvikShareService  {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final String authority;
    private final boolean debug;

    public DalvikShareService(Activity activity) {
        this.activity = activity;
        authority = activity.getPackageName() + ".fileprovider";
        debug = Util.isDebug();
    }

    private void shareText(String subject, String contentText) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        if (subject != null && !subject.isEmpty()) {
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (contentText != null && !contentText.isEmpty()) {
            sharingIntent.putExtra(Intent.EXTRA_TEXT, contentText);
        } else {
            Log.e(TAG, "Error: A non empty contentText is required");
            return;
        }
        if (debug) {
            Log.v(TAG, "Start text sharing intent");
        }
        activity.startActivity(Intent.createChooser(sharingIntent, null));
    }

    private void shareFile(String subject, String contentText, String type, String fileName) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if (subject != null && !subject.isEmpty()) {
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (contentText != null && !contentText.isEmpty()) {
            sharingIntent.putExtra(Intent.EXTRA_TEXT, contentText);
        }
        if (type != null && !type.isEmpty()) {
            sharingIntent.setType(type);
        } else {
            Log.e(TAG, "Error: A non empty type is required");
            return;
        }
        File file = new File(fileName);
        if (file != null && file.exists()) {
            if (debug) {
                Log.v(TAG, String.format("File to share: %s", file));
                Log.v(TAG, "Application name provider: " + authority);
            }
            final Uri uriFile = FileProvider.getUriForFile(activity, authority, file);
            if (debug) {
                Log.v(TAG, String.format("Shared file URI: %s", file));
            }
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uriFile);
            sharingIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            Log.e(TAG, "Error: A non empty file is required");
            return;
        }
        if (debug) {
            Log.v(TAG, "Start file sharing intent");
        }
        activity.startActivity(Intent.createChooser(sharingIntent, null));
    }

}

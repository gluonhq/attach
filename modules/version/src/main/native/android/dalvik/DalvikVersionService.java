/*
 * Copyright (c) 2021, Gluon
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
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

public class DalvikVersionService {

    private static final String TAG = Util.TAG;
    private static final boolean debug = Util.isDebug();

    private final Activity activity;

    public DalvikVersionService(Activity activity) {
        this.activity = activity;
    }

    private String getVersion() {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String versionNumber = packageInfo.versionName;
            if (debug) {
                Log.v(TAG, "Version Service: Version name: " + versionNumber);
            }
            return versionNumber;
        } catch (PackageManager.NameNotFoundException ex) {
            Log.v(TAG, "Error retrieving version name: " + ex);
            return "-1";
        }
    }

    private String getBuild() {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                // deprecated
                versionCode = packageInfo.versionCode;
            }
            String buildNumber = String.valueOf(versionCode);
            if (debug) {
                Log.v(TAG, "Version Service: Version code: " + buildNumber);
            }
            return buildNumber;
        } catch (PackageManager.NameNotFoundException ex) {
            Log.v(TAG, "Error retrieving version code: " + ex);
            return "-1";
        }
    }
}
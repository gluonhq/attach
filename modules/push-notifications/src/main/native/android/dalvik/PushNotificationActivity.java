/*
 * Copyright (c) 2016, 2020, Gluon
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PushNotificationActivity extends Activity {

    private static final String TAG = Util.TAG;
    private static final boolean debug = Util.isDebug();

    public static final String MESSAGE = "message";
    public static final String PACKAGE_NAME = "packageName";
    private String message = "";
    private String packageName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        message = getIntent().getStringExtra(MESSAGE);
        packageName = getIntent().getStringExtra(PACKAGE_NAME);

        final Activity activity = DalvikPushNotificationsService.getActivity();
        if (activity != null && activity.getPackageName().equals(packageName)) {
            // if we have an app running on front or in the background
            if (debug) {
                Log.v(TAG, "PushNotificationActivity - Fire MESSAGE: " + message);
            }
            DalvikPushNotificationsService.sendRuntimeArgs(message);
        } else {
            // we can open the application otherwise
            if (!openApp(this, packageName)) {
                Log.e(TAG, "PushNotificationActivity - Error opening app: " + packageName);
            }
        }
        finish();
    }

    public boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent intent = manager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                Log.e(TAG, "PushNotificationActivity :: App error in package: " + packageName);
                return false;
            }
            Log.v(TAG, "PushNotificationActivity - Set property " + DalvikPushNotificationsService.LAUNCH_PUSH_NOTIFICATION_KEY + " with value: " + message);
            System.setProperty(DalvikPushNotificationsService.LAUNCH_PUSH_NOTIFICATION_KEY, message);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "PushNotificationActivity :: App error: " + e.getMessage());
            return false;
        }
    }


}



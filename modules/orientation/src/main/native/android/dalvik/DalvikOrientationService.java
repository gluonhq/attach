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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;

public class DalvikOrientationService {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final OrientationReceiver orientationReceiver;
    private final IntentFilter intentFilter;
    private boolean isRegistered = false;

    public DalvikOrientationService(Activity activity) {
        this.activity = activity;
        orientationReceiver = new OrientationReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        Util.setLifecycleEventHandler(new LifecycleEventHandler() {
            @Override
            public void lifecycleEvent(String event) {
                if (event != null && !event.isEmpty()) {
                    switch (event) {
                        case "pause":
                            unregisterReceiver();
                            break;
                        case "resume":
                            registerReceiver();
                            sendCurrentOrientation();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        registerReceiver();
        sendCurrentOrientation();
    }

    private void sendCurrentOrientation() {
        String orientation;
        switch (activity.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: orientation = "Landscape"; break;
            case Configuration.ORIENTATION_PORTRAIT: orientation = "Portrait"; break;
            default: orientation = "Unknown";
        }
        if (Util.isDebug()) {
            Log.v(TAG, "Current orientation: " + orientation);
        }
        nativeOrientation(orientation);
    }

    private void registerReceiver() {
        if (!isRegistered) {
            activity.registerReceiver(orientationReceiver, intentFilter);
            isRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isRegistered) {
            activity.unregisterReceiver(orientationReceiver);
            isRegistered = false;
        }
    }

    private native void nativeOrientation(String orientation);

    class OrientationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sendCurrentOrientation();
        }

    }
}
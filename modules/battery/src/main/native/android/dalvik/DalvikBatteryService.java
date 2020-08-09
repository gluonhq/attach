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
import android.os.BatteryManager;
import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
import android.util.Log;

public class DalvikBatteryService {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final BatteryReceiver batteryReceiver;
    private final IntentFilter intentFilter;
    private boolean isRegistered = false;

    public DalvikBatteryService(Activity activity) {
        this.activity = activity;
        batteryReceiver = new BatteryReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
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
                            sendCurrentBattery();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        registerReceiver();
        sendCurrentBattery();
    }

    private void sendCurrentBattery() {
        Intent intent = activity.registerReceiver(null, intentFilter);
        sendCurrentBattery(batteryLevel(intent), isPluggedIn(intent));
    }

    private void sendCurrentBattery(float batteryLevel, boolean pluggedIn) {
        if (Util.isDebug()) {
            Log.v(TAG, "Current battery level: " + batteryLevel + ", status: " + (pluggedIn ? "plugged" : "unplugged"));
        }
        nativeBattery(batteryLevel, pluggedIn);
    }

    private boolean isPluggedIn(Intent batteryIntent) {
        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BATTERY_PLUGGED_AC;
        return usbCharge || acCharge;
    }
    
    private float batteryLevel(Intent batteryIntent) {
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (float) level / (float) scale;
    }

    private void registerReceiver() {
        if (!isRegistered) {
            activity.registerReceiver(batteryReceiver, intentFilter);
            isRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isRegistered) {
            activity.unregisterReceiver(batteryReceiver);
            isRegistered = false;
        }
    }

    private native void nativeBattery(float battery, boolean pluggedIn);

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sendCurrentBattery(batteryLevel(intent), isPluggedIn(intent));
        }

    }
}
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
import static android.content.Context.SENSOR_SERVICE;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class DalvikAccelerometerService implements SensorEventListener {

    private static final String TAG = Util.TAG;

    private final SensorManager sensorManager;
    private boolean isRegistered = false;

    private final double alpha = 0.8;
    private final double[] gravity = new double[3];
    private final boolean debug;
    private boolean filterGravity;
    private int frequency;

    public DalvikAccelerometerService(Activity activity) {
        debug = Util.isDebug();
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        Util.setLifecycleEventHandler(new LifecycleEventHandler() {
            @Override
            public void lifecycleEvent(String event) {
                if (event != null && !event.isEmpty()) {
                    switch (event) {
                        case "pause":
                            unregisterListener();
                            break;
                        case "resume":
                            registerListener();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        registerListener();
    }

    private void setup(boolean filterGravity, int frequency) {
        if (debug) {
            Log.v(TAG, "DalvikAccelerometerService::setup");
        }
        this.filterGravity = filterGravity;
        if (this.frequency != frequency) {
            this.frequency = frequency;
            unregisterListener();
            registerListener();
        }
    }

    private void registerListener() {
        if (!isRegistered) {
            if (debug) {
                Log.v(TAG, "DalvikAccelerometerService::registerListener");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                int rateInMillis = frequency > 0 ? (int) (1000d / (double) frequency) : 20;
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        rateInMillis);
            } else {
                Log.e(TAG, "No accelerometer available");
            }
            isRegistered = true;
        }
    }

    private void unregisterListener() {
        if (isRegistered) {
            if (debug) {
                Log.v(TAG, "DalvikAccelerometerService::unregisterListener");
            }
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (filterGravity) {
                // filter to remove gravity
                gravity[0] = alpha * gravity[0] + (1 - alpha) * se.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * se.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * se.values[2];
            }
            double x = se.values[0] - gravity[0];
            double y = se.values[1] - gravity[1];
            double z = se.values[2] - gravity[2];
            double timeInMills = System.currentTimeMillis() + (se.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;
            notifyAcceleration(x, y, z, timeInMills);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private native void notifyAcceleration(double x, double y, double z, double t);
}
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
import android.util.Log;

public class DalvikMagnetometerService implements SensorEventListener {

    private static final String TAG = Util.TAG;
    private static final float ALPHA = 0.1f;

    private final SensorManager sensorManager;
    private boolean sensorReady = false;
    private boolean isRegistered = false;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private float[] tmpAccelerometerReading = new float[3];
    private float[] tmpMagnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final boolean debug;
    private int frequency;

    public DalvikMagnetometerService(Activity activity) {
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

    private void setup(int frequency) {
        if (debug) {
            Log.v(TAG, "DalvikMagnetometerService::setup");
        }
        if (this.frequency != frequency) {
            this.frequency = frequency;
            unregisterListener();
            registerListener();
        }
    }

    private void registerListener() {
        if (!isRegistered) {
            if (debug) {
                Log.v(TAG, "DalvikMagnetometerService::registerListener");
            }
            int rateInMillis = frequency > 0 ? (int) (1000d / (double) frequency) : 100;
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        rateInMillis);
            } else {
                Log.e(TAG, "No accelerometer available");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                        rateInMillis);
            } else {
                Log.e(TAG, "No magnetometer available");
            }
            isRegistered = true;
        }
    }

    private void unregisterListener() {
        if (isRegistered) {
            if (debug) {
                Log.v(TAG, "DalvikMagnetometerService::unregisterListener");
            }
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(se.values, 0, tmpAccelerometerReading, 0, accelerometerReading.length);
            accelerometerReading = lowPass(tmpAccelerometerReading, accelerometerReading);
        } else if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(se.values, 0, tmpMagnetometerReading, 0, magnetometerReading.length);
            magnetometerReading = lowPass(tmpMagnetometerReading, magnetometerReading);
            sensorReady = true;
        }

        if (sensorReady) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles);

                notifyReading(magnetometerReading[0], magnetometerReading[1], magnetometerReading[2],
                        getMagnitude(),
                        orientationAngles[0], orientationAngles[1], orientationAngles[2]);
            }
            sensorReady = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private double getMagnitude() {
        return Math.sqrt(magnetometerReading[0] * magnetometerReading[0] +
                magnetometerReading[1] * magnetometerReading[1] +
                magnetometerReading[2] * magnetometerReading[2]);
    }

    /**
     * low pass filter
     * See http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * See http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    private float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    private native void notifyReading(double x, double y, double z, double m, double a, double p, double r);
}
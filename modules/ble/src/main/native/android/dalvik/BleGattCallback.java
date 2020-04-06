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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import java.util.Arrays;
import java.util.List;
import android.util.Log;

class BleGattCallback extends BluetoothGattCallback {

    private static final String TAG = "GluonAttach";

    private final Activity activity;
    private final BluetoothDevice bluetoothDevice;
    private BluetoothGatt connectedGatt;

    private boolean connected;

    public BleGattCallback(Activity activity, BluetoothDevice bluetoothDevice) {
        this.activity = activity;
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.v(TAG, "BLE gatt changed from status: " + status + " to status:" + newState);
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
//                Platform.runLater(() -> device.setState(State.STATE_CONNECTED));
                Log.v(TAG, "STATE_CONNECTED");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
//                Platform.runLater(() -> device.setState(State.STATE_DISCONNECTED));
                Log.v(TAG, "STATE_DISCONNECTED");
                break;
            case BluetoothProfile.STATE_CONNECTING:
//                Platform.runLater(() -> device.setState(State.STATE_CONNECTING));
                Log.v(TAG, "STATE_CONNECTING");
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
//                Platform.runLater(() -> device.setState(State.STATE_DISCONNECTING));
                Log.v(TAG, "STATE_DISCONNECTING");
                break;
            default:
//                Platform.runLater(() -> device.setState(State.STATE_UNKWONN));
                Log.v(TAG, "STATE_OTHER");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        List<BluetoothGattService> services = gatt.getServices();
        Log.v(TAG, "onServicesDiscovered: " + services.size() + " services");
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.v(TAG, "onCharacteristicRead read characteristic " + characteristic + ", with status: " + status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.v(TAG, "onCharacteristicWrite write characteristic " + characteristic + ", with status: " + status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.v(TAG, "onCharacteristicChanged characteristic " + characteristic + " changed with value: " + Arrays.toString(characteristic.getValue()));
    }

    void connect() {
        if (bluetoothDevice != null) {
//            Platform.runLater(() -> device.profilesProperties().clear());
            connectedGatt = bluetoothDevice.connectGatt(activity, true, this);
            connected = connectedGatt != null;
        }
    }

    void disconnect() {
        if (connectedGatt != null) {
            connectedGatt.disconnect();
            connected = false;
        }
    }

    boolean isConnected() {
        return connected;
    }

}
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
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class BleGattCallback extends BluetoothGattCallback {

    private static final String TAG = Util.TAG;

    private final Activity activity;
    private final BluetoothDevice bluetoothDevice;
    private final boolean debug;
    private BluetoothGatt connectedGatt;

    private boolean connected;

    public BleGattCallback(Activity activity, BluetoothDevice bluetoothDevice) {
        this.activity = activity;
        this.bluetoothDevice = bluetoothDevice;
        this.debug = Util.isDebug();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (debug) {
            Log.v(TAG, "BLE gatt changed from status: " + status + " to status:" + newState);
        }
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                if (debug) {
                    Log.v(TAG, "STATE_CONNECTED");
                }
                setState(bluetoothDevice.getName(), "STATE_CONNECTED");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                if (debug) {
                    Log.v(TAG, "STATE_DISCONNECTED");
                }
                setState(bluetoothDevice.getName(), "STATE_DISCONNECTED");
                break;
            case BluetoothProfile.STATE_CONNECTING:
                if (debug) {
                    Log.v(TAG, "STATE_CONNECTING");
                }
                setState(bluetoothDevice.getName(), "STATE_CONNECTING");
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                if (debug) {
                    Log.v(TAG, "STATE_DISCONNECTING");
                }
                setState(bluetoothDevice.getName(), "STATE_DISCONNECTING");
                break;
            default:
                if (debug) {
                    Log.v(TAG, "STATE_OTHER");
                }
                setState(bluetoothDevice.getName(), "STATE_OTHER");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        List<BluetoothGattService> services = gatt.getServices();
        if (debug) {
            Log.v(TAG, "onServicesDiscovered: " + services.size() + " services");
        }
        for (BluetoothGattService service : services) {
            if (debug) {
                Log.v(TAG, "BLE, service: " + service + ", with uuid: " + service.getUuid().toString());
            }
            addProfile(bluetoothDevice.getName(), service.getUuid().toString(), service.getType() == 0 ? "Primary Service" : "Secondary Service");
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {}

        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (debug) {
                    Log.v(TAG, "  BLE, char = " + characteristic + " with uuid: " + characteristic.getUuid().toString());
                }
                addCharacteristic(bluetoothDevice.getName(), service.getUuid().toString(),
                        characteristic.getUuid().toString(), getProperties(characteristic.getProperties()));
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {}

        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    byte[] value = descriptor.getValue() != null ? descriptor.getValue() : new byte[]{};
                    if (debug) {
                        Log.v(TAG, "    BLE, char = " + characteristic + " with descriptor uuid: " + descriptor.getUuid().toString() + " , value: " + Arrays.toString(value));
                    }
                    addDescriptor(bluetoothDevice.getName(), service.getUuid().toString(),
                            characteristic.getUuid().toString(), descriptor.getUuid().toString(), value);
                }
            }
        }
    }

    void read(String profile, String characteristic) {
        if (connectedGatt == null) {
            Log.e(TAG, "BLE READ failed: connectedGatt was null");
            return;
        }
        final BluetoothGattService service1 = connectedGatt.getService(UUID.fromString(profile));
        if (service1 == null) {
            Log.e(TAG, "BLE READ failed: no service with " + profile);
            return;
        }
        BluetoothGattCharacteristic characteristic1 = service1.getCharacteristic(UUID.fromString(characteristic));
        if (characteristic1 == null) {
            Log.e(TAG, "BLE READ failed: no characteristic found with " + characteristic);
            return;
        }
        if (debug) {
            Log.v(TAG, "Reading characteristic " + characteristic1.getUuid().toString());
        }
        connectedGatt.readCharacteristic(characteristic1);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (debug) {
            Log.v(TAG, "onCharacteristicRead read characteristic " + characteristic + ", with status: " + status);
        }
        byte[] value = characteristic.getValue() != null ? characteristic.getValue() : new byte[]{};
        setValue(bluetoothDevice.getName(), characteristic.getUuid().toString(), value);
    }

    void write(String profile, String characteristic, byte[] value) {
        if (connectedGatt == null) {
            Log.e(TAG, "BLE WRITE failed: connectedGatt was null");
            return;
        }
        final BluetoothGattService service1 = connectedGatt.getService(UUID.fromString(profile));
        if (service1 == null) {
            Log.e(TAG, "BLE WRITE failed: no service with " + profile);
            return;
        }
        BluetoothGattCharacteristic characteristic1 = service1.getCharacteristic(UUID.fromString(characteristic));
        if (characteristic1 == null) {
            Log.e(TAG, "BLE WRITE failed: no characteristic found with " + characteristic);
            return;
        }
        if (debug) {
            Log.v(TAG, "Writing characteristic " + characteristic1.getUuid().toString());
        }
        characteristic1.setValue(value);
        connectedGatt.writeCharacteristic(characteristic1);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (debug) {
            Log.v(TAG, "onCharacteristicWrite write characteristic " + characteristic + ", with status: " + status);
        }
        byte[] value = characteristic.getValue() != null ? characteristic.getValue() : new byte[]{};
        setValue(bluetoothDevice.getName(), characteristic.getUuid().toString(), value);
    }

    void subscribe(String profile, String characteristic, boolean subscribe) {
        if (connectedGatt == null) {
            Log.e(TAG, "BLE SUBSCRIBE failed: connectedGatt was null");
            return;
        }
        final BluetoothGattService service1 = connectedGatt.getService(UUID.fromString(profile));
        if (service1 == null) {
            Log.e(TAG, "BLE SUBSCRIBE failed: no service with " + profile);
            return;
        }
        BluetoothGattCharacteristic characteristic1 = service1.getCharacteristic(UUID.fromString(characteristic));
        if (characteristic1 == null) {
            Log.e(TAG, "BLE SUBSCRIBE failed: no characteristic found with " + characteristic);
            return;
        }
        if (!connectedGatt.setCharacteristicNotification(characteristic1, true)) {
            Log.e(TAG, "BLE SUBSCRIBE failed for characteristic " + characteristic1.getUuid().toString());
        }

        // Enable notification descriptor
        for (BluetoothGattDescriptor descriptor : characteristic1.getDescriptors()) {
            if (descriptor != null) {
                byte[] value = subscribe ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                if (debug) {
                    Log.v(TAG, "subscribe characteristic " + characteristic1 + ", with value: " + Arrays.toString(value));
                }
                descriptor.setValue(value);
                connectedGatt.writeDescriptor(descriptor);
                addDescriptor(bluetoothDevice.getName(), service1.getUuid().toString(),
                        characteristic1.getUuid().toString(), descriptor.getUuid().toString(), value);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue() != null ? characteristic.getValue() : new byte[]{};
        if (debug) {
            Log.v(TAG, "onCharacteristicChanged characteristic " + characteristic + " changed with value: " + Arrays.toString(value));
        }
        setValue(bluetoothDevice.getName(), characteristic.getUuid().toString(), value);
    }

    void connect() {
        if (bluetoothDevice != null) {
            connectedGatt = bluetoothDevice.connectGatt(activity, false, this);
            if (connectedGatt != null) {
                setState(bluetoothDevice.getName(), "STATE_CONNECTING");
                connected = connectedGatt.connect();
            }
        }
    }

    void disconnect() {
        if (connectedGatt != null) {
            connectedGatt.disconnect();
            connectedGatt = null;
            connected = false;
        }
    }

    boolean isConnected() {
        return connected;
    }

    private String getProperties(int bitmask) {
        String properties = "";

        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0) {
            properties += "broadcast, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0) {
            properties += "extended props, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            properties += "indicate, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            properties += "notify, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            properties += "read, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {
            properties += "signed write, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            properties += "write, ";
        }
        if ((bitmask & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            properties += "write no response, ";
        }

        return properties.isEmpty() ? "" : properties.substring(0, properties.length() - 2);
    }

    // native
    private native void setState(String name, String state);
    private native void addProfile(String name, String uuid, String type);
    private native void addCharacteristic(String name, String profileUuid, String charUuid, String properties);
    private native void addDescriptor(String name, String profileUuid, String charUuid, String descUuid, byte[] value);
    private native void setValue(String name, String charUuid, byte[] value);

}
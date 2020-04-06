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
import static android.app.Activity.RESULT_OK;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseCallback;

import android.content.Intent;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

public class DalvikBleService  {

    private static final String TAG = "GluonAttach";
    private final Activity activity;
    private static final Logger LOG = Logger.getLogger(DalvikBleService.class.getName());
    private BluetoothLeScanner scanner;

    private final static int REQUEST_ENABLE_BT = 1001;
    private final List<String> uuids = new LinkedList<>();
    private AdvertiseCallback callback;

    public DalvikBleService(Activity a) {
        this.activity = a;
        init();
    }

    private void init() {
        Log.v(TAG, "DalvikBle, init");
        boolean fineloc = Util.verifyPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!fineloc) {
            Log.v(TAG, "No permission to get fine location");
        }
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            Log.v(TAG, "DalvikBle, init, adapter not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            IntentHandler intentHandler = new IntentHandler() {
                @Override
                public void gotActivityResult (int requestCode, int resultCode, Intent intent) {
                    if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
                        scanner = adapter.getBluetoothLeScanner();
                    }
                }
            };

            if (activity == null) {
                LOG.log(Level.WARNING, "Activity not found. This service is not allowed when "
                        + "running in background mode or from wearable");
                return;
            }
            Util.setOnActivityResultHandler(intentHandler);

            // A dialog will appear requesting user permission to enable Bluetooth
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            scanner = adapter.getBluetoothLeScanner();
        }

    }

    private ScanCallback scanCallback;

    private void startScanning() {
        Log.v(TAG, "BleService startScanning\n");
        if (scanner == null) {
            Log.e(TAG, "Scanner still null");
            return;
        }
        this.scanCallback = createCallback();
        scanner.startScan(scanCallback);
    }

    private void stopScanning() {
        Log.v(TAG, "BleService stopScanning\n");
        if (scanner != null && scanCallback != null) {
            scanner.stopScan(scanCallback);
        }
    }

    private void startBroadcast(String uuid, int major, int minor, String id) {
        Log.v(TAG, "Start broadcasting for uuid = "+uuid+", major = "+major+", minor = "+minor+", id = "+id);
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings parameters = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        byte[] payload = getPayload(uuid, major, minor);
        AdvertiseData data = new AdvertiseData.Builder()
                .addManufacturerData(0x004C, payload)
                .build();
        callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.v(TAG, "onStartSuccess");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        Log.v(TAG, "Advertise with payload = " + Arrays.toString(payload));
        advertiser.startAdvertising(parameters, data, callback);
    }

    private void stopBroadcast() {
        Log.v(TAG, "Stop broadcasting");
        if (callback != null) {
            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            advertiser.stopAdvertising(callback);
            callback = null;
        }
    }

    private ScanCallback createCallback() {
        ScanCallback answer = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.v(TAG, "BLESERVICE: onScanResult, callbacktype = " + callbackType);
                ScanRecord mScanRecord = result.getScanRecord();
                byte[] scanRecord = mScanRecord.getBytes();

                // https://www.pubnub.com/blog/2015-04-14-building-android-beacon-android-ibeacon-tutorial-overview/
                int index = 0;
                while (index < scanRecord.length) {
                    int length = scanRecord[index++];
                    if (length == 0) {
                        break;
                    }

                    int type = (scanRecord[index] & 0xff);

                    // process data
                    if (type == 0xff) {
                        processAD(scanRecord, index + 1, result.getRssi());
                    }

                    index += length;
                }
            }
        };
        return answer;
    }

   private void processAD(byte[] scanRecord, int init, int mRssi) {
        // AD: (Length FF) mID0 mID1 bID1 bID0 uuid15 ... uuid0 M1 M0 m1 m0 tx
        int startByte = init;
        // Manufacturer ID (little endian)
        // https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
        int mID = ((scanRecord[startByte+1] & 0xff) << 8) | (scanRecord[startByte] & 0xff);

        startByte += 2;
        // Beacon ID (big endian)
        int beaconID = ((scanRecord[startByte] & 0xff) << 8) | (scanRecord[startByte+1] & 0xff);
        startByte += 2;
        // UUID (big endian)
        byte[] uuidBytes = Arrays.copyOfRange(scanRecord, startByte, startByte+16);
        String scannedUuid = ByteArrayToUUIDString(uuidBytes);

        if (uuids.isEmpty() || uuids.contains(scannedUuid.toLowerCase())) {
            startByte += 16;
            // major (big endian)
            int major = ((scanRecord[startByte] & 0xff) << 8) | (scanRecord[startByte+1] & 0xff);
            startByte += 2;
            // minor (big endian)
            int minor = ((scanRecord[startByte] & 0xff) << 8) | (scanRecord[startByte+1] & 0xff);
            startByte += 2;
            // power in dB
            int power = (scanRecord[startByte] & 0xff);
            power -= 256;
            int proximity = calculateProximity(power, mRssi);

            Log.v(TAG, "Scan: mID: "+mID+", beaconID: "+beaconID+", uuid: "+scannedUuid+
                    ", major: "+major+", minor: "+minor+", power: "+power+", distance: "+proximity);
            scanDetected (scannedUuid, major, minor, power, 0);
        }
    }

    private static String ByteArrayToUUIDString(byte[] ba) {
        StringBuilder hex = new StringBuilder();
        for (byte b : ba) {
            hex.append(new Formatter().format("%02x", b));
        }
        return hex.toString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5" );
    }

    private static int calculateProximity (int txPower, double rssi) {
        double accuracy = calculateAccuracy(txPower, rssi);
        Log.v(TAG, "accuracy = "+accuracy+", power = "+txPower+", rssi = "+rssi);
        if (accuracy < 0) {
            return 0;
        }
        if (accuracy < 0.5) {
            return 1;
        }
        if (accuracy < 4) {
            return 2;
        }
        return 3;
    }

    private static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0 || txPower == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = 0.89976 * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    private static byte[] getPayload(String uuid, int major, int minor) {
        byte[] prefixArray = getBytesFromShort((short) 533);
        byte[] uuidArray = getBytesFromUUID(uuid);
        byte[] majorArray = getBytesFromShort((short) major);
        byte[] minorArray = getBytesFromShort((short) minor);
        byte[] powerArray = {(byte) -69};

        byte[] allByteArray = new byte[prefixArray.length + uuidArray.length + majorArray.length + minorArray.length + powerArray.length];

        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(prefixArray);
        buff.put(uuidArray);
        buff.put(majorArray);
        buff.put(minorArray);
        buff.put(powerArray);

        return buff.array();
    }

    private static byte[] getBytesFromUUID(String uuidString) {
        final UUID uuid = UUID.fromString(uuidString);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    private static byte[] getBytesFromShort(short value) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[2]);
        buffer.putShort(value);
        return buffer.array();
    }

    private native void scanDetected(String uuid, int major, int minor, int rsi, int proxy);



}

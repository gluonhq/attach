/*
 * Copyright (c) 2016, 2020 Gluon
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
package com.gluonhq.attach.ble;

import com.gluonhq.attach.util.Services;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * <p><b>Beacons</b></p>
 *
 * The BLE scanner, which is short for Bluetooth Low Energy, can be used to communicate
 * with devices that are equipped with Bluetooth low energy wireless technology. After
 * scanning has started, a callback function will be called with a scanned result from
 * a detected nearby BLE enabled device. The scan result will contain the major and minor
 * values of the detected device, as well as its signal strength (rssi) and an approximation
 * of the proximity to the scanning device.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code String uuid = UUID.randomUUID().toString(); // for example a known UUID of a beacon
 *  BleService.create().ifPresent(service -> {
 *      service.startScanning(new Configuration(uuid), scanResult -> {
 *          System.out.printf("major: %d, minor: %d, proximity: %s",
 *                  scanResult.getMajor(), scanResult.getMinor(),
 *                  scanResult.getProximity().name());
 *      });
 *  });}</pre>
 *
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permissions <code>android.permission.BLUETOOTH</code> and
 * <code>android.permission.BLUETOOTH_ADMIN</code> need to be added.</p>
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.BLUETOOTH"/>
 *    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
 *    ...
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 * <p>The following keys are required for Beacons:</p>
 * <pre>
 * {@code <key>NSLocationUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 6+)</string>
 *  <key>NSLocationAlwaysUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 8+)</string>
 *  <key>NSBluetoothAlwaysUsageDescription</key>
 *  <string>Reason to use Bluetooth interface (iOS 13+)</string>}</pre>
 *
 * @since 3.9.0
 *
 * <p><b>Devices</b></p>
 *
 * BLE devices are equipped with Bluetooth low energy wireless technology, such as
 * heart rate monitors and digital thermostats. This BleService can be added to
 * central devices (i.e. mobile device with Bluetooth enabled), to discover other
 * peripheral BLE devices. The central can request to connect to the peripheral so
 * it can explore and interact with its data. The peripheral is responsible for
 * responding to the central in appropriate ways.
 *
 * For more info about BLE see:
 * https://www.bluetooth.com/specifications/generic-attributes-overview
 *
 * <p><b>Example</b></p>
 * <p>This code snippet shows how to discover devices:</p>
 * <pre>
 * {@code
 * BleService.create().ifPresent(ble -> {
 *        ble.startScanningDevices().addListener((ListChangeListener.Change<? extends BleDevice> c) -> {
 *            while (c.next()) {
 *                if (c.wasAdded()) {
 *                    for (BleDevice device : c.getAddedSubList()) {
 *                        System.out.println("Device found: " + device.getName());
 *                        device.stateProperty().addListener((obs, ov, nv) -> {
 *                            if (State.STATE_CONNECTED == nv) {
 *                                System.out.println("Device connected: " + device.getName());
 *                            }
 *                        });
 *                        ble.connect(device);
 *                    }
 *                }
 *            }
 *        });
 *    });
 * }</pre>
 *
 * <p>This code snippet shows how to discover the services for a given device:</p>
 * <pre>
 * {@code
 * BleService.create().ifPresent(ble -> {
 *        ...
 *        bleDevice.getProfiles().addListener((ListChangeListener.Change<? extends BleProfile> c) -> {
 *            while (c.next()) {
 *                if (c.wasAdded()) {
 *                    for (BleProfile profile : c.getAddedSubList()) {
 *                        int assignedNumber = BleSpecs.getAssignedNumber(profile.getUuid());
 *                        String specificationName = BleSpecs.GattServices.ofAssignedNumber(assignedNumber).getSpecificationName();
 *                        System.out.println("Profile: " + specificationName + ", " + String.format("UUID: 0x%04x", assignedNumber));
 *                    }
 *                }
 *            }
 *        });
 *    });
 * }</pre>
 *
 * <p>This code snippet shows how to discover the characteristics for a given service:</p>
 * <pre>
 * {@code
 * BleService.create().ifPresent(ble -> {
 *        ...
 *        bleProfile.getCharacteristics().addListener((ListChangeListener.Change<? extends BleCharacteristic> c) -> {
 *            while (c.next()) {
 *                if (c.wasAdded()) {
 *                    for (BleCharacteristic characteristic : c.getAddedSubList()) {
 *                        int assignedNumber = BleSpecs.getAssignedNumber(characteristic.getUuid());
 *                        String specificationName = BleSpecs.GattServices.ofAssignedNumber(assignedNumber).getSpecificationName();
 *                        System.out.println("Characteristic: " + specificationName + ", " + String.format("UUID: 0x%04x", assignedNumber));
 *                    }
 *                }
 *            }
 *        });
 *    });
 * }</pre>
 *
 * <p>And finally, this code snippet shows how to subscribe to a characteristic of a given device and service to listen to its values:</p>
 * <pre>
 * {@code
 * BleService.create().ifPresent(ble -> {
 *        ...
 *        bleCharacteristic.valueProperty().addListener((obs, ov, nv) ->
 *              System.out.println("Value: " + Arrays.toString(nv)));
 *        ble.subscribeCharacteristic(bleDevice, bleProfile.getUuid(), bleCharacteristic.getUuid());
 *    });
 * }</pre>
 *
 *
 * <p><b>Android Configuration</b></p>
 * <p>The same permissions <code>android.permission.BLUETOOTH</code>,
 * <code>android.permission.BLUETOOTH_ADMIN</code> and
 * <code>android.permission.ACCESS_FINE_LOCATION</code> need to be added to the Android manifest.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.BLUETOOTH"/>
 *    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
 *    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 *    ...
 *    <activity android:name="com.gluonhq.helloandroid.PermissionRequestActivity" />
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 *
 * <p>The following keys are required for Devices:</p>
 * <pre>
 * {@code <key>NSBluetoothAlwaysUsageDescription</key>
 *  <string>Reason to use Bluetooth interface (iOS 13+)</string>}</pre>
 *
 * @since 4.0.6
 *
 */
public interface BleService {

    /**
     * Returns an instance of {@link BleService}.
     * @return An instance of {@link BleService}.
     */
    static Optional<BleService> create() {
        return Services.get(BleService.class);
    }

    // BEACONS

    /**
     * Start scanning for BLE beacons. When such a device is discovered, the callback will
     * be called with the detailed information on the detected beacon. Note that this method
     * can be called multiple times. In order to stop receiving notifications, the stopScanning method
     * should be used.
     * @param configuration provide setting options to filter the beacons to be scanned
     * @param callback provided function that will be called once a beacon is detected
     */
    // TODO: add filtering options, so if more than one beacon is found, apply them to select which one will be picked
    void startScanning(Configuration configuration, Consumer<ScanDetection> callback);

    /**
     * Stops the last called startScanning operation.
     */
    void stopScanning();

    /**
     * Configure the current device as a Bluetooth beacon, and start
     * advertising with a given UUID
     *
     * @param beaconUUID the UUID of the beacon that will be advertised
     * @param major the most significant value
     * @param minor the least significant value
     * @param identifier a string to identify the beacon
     * @since 4.0.7
     */
    void startBroadcasting(UUID beaconUUID, int major, int minor, String identifier);

    /**
     * Stop advertising the current iOS device as a Bluetooth beacon
     *
     * @since 4.0.7
     */
    void stopBroadcasting();

    // DEVICES

    /**
     * Start scanning for BLE Devices.
     *
     * @return an observable list of {@link BleDevice} found
     * @since 4.0.6
     */
    ObservableList<BleDevice> startScanningDevices();

    /**
     * Stops scanning for BLE devices
     */
    void stopScanningDevices();

    /**
     * Connects to a given BLE device
     * @param device The BleDevice to connect to
     * @since 4.0.6
     */
    void connect(BleDevice device);

    /**
     * Disconnects from a given BLE device
     * @param device The BleDevice to disconnect from
     * @since 4.0.6
     */
    void disconnect(BleDevice device);

    /**
     * Given a BleDevice, with a given BleProfile and a given BleCharacteristic,
     * reads its value
     *
     * @param device The connected BleDevice
     * @param uuidProfile The UUID that identifies the BLE Profile
     * @param uuidCharacteristic The UUID that identifies the BLE Characteristic
     * @since 4.0.6
     */
    void readCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic);

    /**
     * Given a BleDevice, with a given BleProfile and a given BleCharacteristic,
     * writes its value
     *
     * @param device The connected BleDevice
     * @param uuidProfile The UUID that identifies the BLE Profile
     * @param uuidCharacteristic The UUID that identifies the BLE Characteristic
     * @param value a new value for the Ble Characteristic
     * @since 4.0.6
     */
    void writeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic, byte[] value);

    /**
     * Given a BleDevice, with a given BleProfile and a given BleCharacteristic,
     * subscribes to listen to changes in its value
     *
     * @param device The connected BleDevice
     * @param uuidProfile The UUID that identifies the BLE Profile
     * @param uuidCharacteristic The UUID that identifies the BLE Characteristic
     * @since 4.0.6
     */
    void subscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic);

    /**
     * Given a BleDevice, with a given BleProfile and a given BleCharacteristic,
     * unsubscribes and stop listening to changes in its value
     *
     * @param device The connected BleDevice
     * @param uuidProfile The UUID that identifies the BLE Profile
     * @param uuidCharacteristic The UUID that identifies the BLE Characteristic
     * @since 4.0.6
     */
    void unsubscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic);

}

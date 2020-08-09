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
package com.gluonhq.attach.ble.impl;

import com.gluonhq.attach.ble.BleCharacteristic;
import com.gluonhq.attach.ble.BleDescriptor;
import com.gluonhq.attach.ble.BleDevice;
import com.gluonhq.attach.ble.BleProfile;
import com.gluonhq.attach.ble.BleService;
import com.gluonhq.attach.ble.BleSpecs;
import com.gluonhq.attach.ble.Configuration;
import com.gluonhq.attach.ble.ScanDetection;
import com.gluonhq.attach.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * iOS implementation of BleService
 * 
 * Important note:
 *
 * To scan Beacons, it requires adding to the info.plist file:
 * 
 * {@code 
 *         <key>NSLocationUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 6+)</string>
 *         <key>NSLocationAlwaysUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+)</string>
 *         <key>NSBluetoothAlwaysUsageDescription</key>
 *         <string>Reason to use Bluetooth interface (iOS 13+)</string>
 * }
 *
* To scan Devices, it requires adding to the info.plist file:
 *
 * {@code
 *         <key>NSBluetoothAlwaysUsageDescription</key>
 *         <string>Reason to use Bluetooth interface (iOS 13+)</string>
 * }
*/
public class IOSBleService implements BleService {

    private static final Logger LOG = Logger.getLogger(IOSBleService.class.getName());

    static {
        System.loadLibrary("Ble");
        initBle();
    }
    
    private static Consumer<ScanDetection> callback;
    private static final ObservableList<BleDevice> devices = FXCollections.observableArrayList();
    private static final List<String> deviceNames = new LinkedList<>();
    private static final List<String> profileNames = new LinkedList<>();
    private static boolean debug;

    public IOSBleService() {
        debug = Util.DEBUG;
    }

    // BLE BEACONS

    /**
     * startScanning is called with a given uuid and a callback for the beacon found.
     * iOS iBeacon only scans for given uuid's
     * 
     * iOS apps using BleService require the use of the key 
     * NSLocationAlwaysDescription in the plist file so the user is 
     * asked about allowing location services
     * 
     * @param region Containing the beacon uuid
     * @param callback Callback added to the beacon
     */

    @Override
    public void startScanning(Configuration region, Consumer<ScanDetection> callback) {
        IOSBleService.callback = callback;
        String[] uuids = new String[region.getUuids().size()];
        uuids = region.getUuids().toArray(uuids);
        startObserver(uuids);
    }

    /**
     * stopScanning, if the manager is initialized
     */
    @Override
    public void stopScanning() {
        stopObserver();
    }

    @Override
    public void startBroadcasting(UUID beaconUUID, int major, int minor, String identifier) {
        startBroadcast(beaconUUID.toString(), major, minor, identifier);
    }

    @Override
    public void stopBroadcasting() {
        stopBroadcast();
    }

    // native
    private static native void initBle(); // init IDs for java callbacks from native
    private static native void startObserver(String[] uuids);
    private static native void stopObserver();
    private static native void startBroadcast(String uuid, int major, int minor, String id);
    private static native void stopBroadcast();

    // callback
    private static void setDetection(String uuid, int major, int minor, int rssi, int proximity) {
        ScanDetection detection = new ScanDetection();
        detection.setUuid(uuid);
        detection.setMajor(major);
        detection.setMinor(minor);
        detection.setRssi(rssi);
        detection.setProximity(proximity);
        Platform.runLater(() -> callback.accept(detection));
    }

    // BLE DEVICES

    @Override
    public ObservableList<BleDevice> startScanningDevices() {
        devices.clear();
        deviceNames.clear();
        profileNames.clear();
        startScanningPeripherals();
        return devices;
    }

    @Override
    public void stopScanningDevices() {
        stopScanningPeripherals();
    }

    @Override
    public void connect(BleDevice device) {
        if (!checkDevice(device)) {
            return;
        }
        profileNames.clear();
        device.getProfiles().clear();
        doConnect(device.getName(), device.getAddress());
    }

    @Override
    public void disconnect(BleDevice device) {
        if (!checkDevice(device)) {
            return;
        }
        doDisconnect(device.getName(), device.getAddress());
    }

    @Override
    public void readCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
        if (!checkDevice(device)) {
            return;
        }

        doRead(device.getName(), BleSpecs.getServiceToken(uuidProfile), BleSpecs.getCharacteristicsToken(uuidCharacteristic));
    }

    @Override
    public void writeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic, byte[] value) {
        if (!checkDevice(device)) {
            return;
        }
        doWrite(device.getName(), BleSpecs.getServiceToken(uuidProfile), BleSpecs.getCharacteristicsToken(uuidCharacteristic), value);
    }

    @Override
    public void subscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
        if (!checkDevice(device)) {
            return;
        }
        doSubscribe(device.getName(), BleSpecs.getServiceToken(uuidProfile), BleSpecs.getCharacteristicsToken(uuidCharacteristic), true);
    }

    @Override
    public void unsubscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
        if (!checkDevice(device)) {
            return;
        }
        doSubscribe(device.getName(), BleSpecs.getServiceToken(uuidProfile), BleSpecs.getCharacteristicsToken(uuidCharacteristic), false);
    }

    private static boolean checkDevice(BleDevice device) {
        if (device == null) {
            return false;
        }
        if (device.getName() == null) {
            if (debug) {
                LOG.log(Level.INFO, "IOSBleService: Device with null name not allowed");
            }
            return false;
        }
        final boolean check = deviceNames.contains(device.getName());
        if (debug) {
            LOG.log(Level.INFO, "IOSBleService: Device with name " + device.getName() + " in device list: " + check);
        }
        return check;
    }

    // native

    private static native void startScanningPeripherals();
    private static native void stopScanningPeripherals();
    private static native void doConnect(String name, String uuid);
    private static native void doDisconnect(String name, String uuid);
    private static native void doRead(String name, String uuidService, String uuidChar);
    private static native void doWrite(String name, String uuidService, String uuidChar, byte[] value);
    private static native void doSubscribe(String name, String uuidService, String uuidChar, boolean subscribe);

    private static void gotPeripheral(String name, String uuid) {
        if ((name != null && deviceNames.contains(name)) ||
                (name == null && uuid != null && deviceNames.contains(uuid))) {
            return;
        }
        if (name != null && uuid != null && deviceNames.contains(uuid)) {
            deviceNames.remove(uuid);
            devices.removeIf(d -> uuid.equals(d.getAddress()));
        }

        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService got peripheral named %s and uuid: %s", name, uuid));
        }
        BleDevice dev = new BleDevice();
        dev.setName(name);
        dev.setAddress(uuid);
        Platform.runLater(() -> devices.add(dev));
        deviceNames.add(name != null ? name : uuid);

    }

    private static void gotState(String name, String state) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE device %s changed state to %s", name, state));
        }

        getDeviceByName(name).ifPresent(device ->
                Platform.runLater(() -> device.setState(BleDevice.State.fromName(state))));
    }

    private static void gotProfile(String name, String serviceName, boolean primary) {
        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService peripheral named %s has service with uuid: %s", name, serviceName));
        }

        getDeviceByName(name).ifPresent(device -> {
            UUID uuid = BleSpecs.getUUIDFromServiceName(serviceName);

            if (!profileNames.contains(uuid.toString())) {
                profileNames.add(uuid.toString());

                // if profile is not included yet:
                BleProfile profile = new BleProfile();
                profile.setUuid(uuid);
                profile.setType(primary ? "Primary Service" : "Secondary Service");
                if (debug) {
                    LOG.log(Level.INFO, String.format("IOSBleService creating profile %s", serviceName));
                }
                Platform.runLater(() -> device.getProfiles().add(profile));
            }
        });
    }

    private static void removeProfile(String name, String serviceName) {
        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService peripheral named %s has removed service with uuid: %s", name, serviceName));
        }

        getDeviceByName(name).ifPresent(device -> {
            UUID uuid = BleSpecs.getUUIDFromServiceName(serviceName);

            if (!profileNames.contains(uuid.toString())) {
                profileNames.remove(uuid.toString());

                Platform.runLater(() -> device.getProfiles().removeIf(s -> uuid.equals(s.getUuid())));
            }
        });
    }

    private static void gotCharacteristic(String name, String serviceName, String charNumber, String properties) {
        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService peripheral named %s has service named: %s and characteristic with uuid: %s", name, serviceName, charNumber));
        }

        getDeviceByName(name).ifPresent(device -> {
            UUID uuid = BleSpecs.getUUIDFromServiceName(serviceName);
            UUID uuidChar = BleSpecs.getUUIDfromTokenOrElse(charNumber, BleSpecs::getUUIDFromCharacteristicsName).orElse(UUID.randomUUID());

            if (profileNames.contains(uuid.toString())) {
                for (BleProfile profile : device.getProfiles()) {
                    if (profile.getUuid().equals(uuid)) {
                        if (debug) {
                            LOG.log(Level.INFO, String.format("IOSBleService updating profile with characteristic %s", charNumber));
                        }
                        boolean exists = false;
                        for (BleCharacteristic c : profile.getCharacteristics()) {
                            if (c.getUuid().equals(uuidChar)) {
                                c.setProperties(properties);
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            BleCharacteristic bleCharacteristic = new BleCharacteristic(uuidChar);
                            bleCharacteristic.setProperties(properties);
                            Platform.runLater(() ->
                                    profile.getCharacteristics().add(bleCharacteristic));
                        }
                        if (debug) {
                            LOG.log(Level.INFO, String.format("IOSBleService DONE updating profile with characteristic %s", charNumber));
                        }
                        return;
                    }
                }
                if (debug) {
                    LOG.log(Level.INFO, String.format("IOSBleService FAILED updating profile with characteristic %s and uuid %s", charNumber, uuidChar));
                }
            }
        });
    }

    private static void gotValue(String name, String charNumber, String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService peripheral named %s has characteristic with uuid: %s and value: %s (%s)", name, charNumber, data, Arrays.toString(bytes)));
        }

        getDeviceByName(name).ifPresent(device -> {
            UUID uuid = BleSpecs.getUUIDfromTokenOrElse(charNumber, BleSpecs::getUUIDFromCharacteristicsName).orElse(UUID.randomUUID());
            for (BleProfile p : device.getProfiles()) {
                for (BleCharacteristic c : p.getCharacteristics()) {
                    if (c.getUuid().equals(uuid)) {
                        c.setValue(bytes);
                        if (debug) {
                            LOG.log(Level.INFO, String.format("IOSBleService DONE updating value with characteristic %s", charNumber));
                        }
                        return;
                    }
                }
            }
            if (debug) {
                LOG.log(Level.INFO, String.format("IOSBleService FAILED updating value with characteristic %s and uuid %s", charNumber, uuid));
            }
        });
    }

    private static void gotDescriptor(String name, String descName, String data) {
        if (debug) {
            LOG.log(Level.INFO, String.format("IOSBleService peripheral named %s has descriptor: %s and data %s", name, descName, data));
        }

        getDeviceByName(name).ifPresent(device -> {
            UUID uuidDesc = BleSpecs.getUUIDFromDescriptorName(descName);
            for (BleProfile p : device.getProfiles()) {
                for (BleCharacteristic c : p.getCharacteristics()) {
                    for (BleDescriptor d : c.getDescriptors()) {
                        if (d.getUuid().equals(uuidDesc)) {
                            d.setValue(data == null ? null : data.getBytes());
                            return;
                        }
                    }
                    BleDescriptor d = new BleDescriptor();
                    d.setUuid(uuidDesc);
                    d.setValue(data == null ? null : data.getBytes());
                    c.getDescriptors().add(d);
                    return;
                }
            }
        });
    }

    private static Optional<BleDevice> getDeviceByName(String name) {
        if (name == null || !deviceNames.contains(name)) {
            return Optional.empty();
        }

        for (BleDevice device : devices) {
            if (name.equals(device.getName())) {
                return Optional.of(device);
            }
        }
        return Optional.empty();
    }
}

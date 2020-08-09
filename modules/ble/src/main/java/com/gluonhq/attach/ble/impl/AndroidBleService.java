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
package com.gluonhq.attach.ble.impl;

import com.gluonhq.attach.ble.BleCharacteristic;
import com.gluonhq.attach.ble.BleDescriptor;
import com.gluonhq.attach.ble.BleDevice;
import com.gluonhq.attach.ble.BleProfile;
import com.gluonhq.attach.ble.BleService;
import com.gluonhq.attach.ble.Configuration;
import com.gluonhq.attach.ble.ScanDetection;
import com.gluonhq.attach.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Android implementation of BleService
 *
 */
public class AndroidBleService implements BleService {

    private static final Logger LOG = Logger.getLogger(AndroidBleService.class.getName());
    private static final ObservableList<BleDevice> devices = FXCollections.observableArrayList();
    private static final List<String> deviceNames = new LinkedList<>();
    private static final List<String> profileNames = new LinkedList<>();
    private static final boolean debug = Util.DEBUG;

    private static Consumer<ScanDetection> callback;

    static {
        LOG.fine("Loading AndroidBleService");
        System.loadLibrary("ble");
        LOG.fine("Loaded AndroidBleService");
    }

    public AndroidBleService() {
        LOG.fine("Created AndroidBleService instance");
    }

    // BLE BEACONS

    @Override
    public void startScanning(Configuration region, Consumer<ScanDetection> callback) {
        LOG.fine("AndroidBleService will start scanning");
        AndroidBleService.callback = callback;
        String[] uuids = new String[region.getUuids().size()];
        uuids = region.getUuids().toArray(uuids);
        startObserver(uuids);
    }

    @Override
    public void stopScanning() {
        LOG.fine("AndroidBleService will stop scanning");
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

    // BLE DEVICES

    @Override
    public ObservableList<BleDevice> startScanningDevices() {
        LOG.fine("AndroidBleService will start scanning devices");
        devices.clear();
        deviceNames.clear();
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
        doRead(device.getAddress(), uuidProfile.toString(), uuidCharacteristic.toString());
    }

    @Override
    public void writeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic, byte[] value) {
        doWrite(device.getAddress(), uuidProfile.toString(), uuidCharacteristic.toString(), value);
    }

    @Override
    public void subscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
        doSubscribe(device.getAddress(), uuidProfile.toString(), uuidCharacteristic.toString(), true);
    }

    @Override
    public void unsubscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
        doSubscribe(device.getAddress(), uuidProfile.toString(), uuidCharacteristic.toString(), false);
    }

    private static boolean checkDevice(BleDevice device) {
        if (device == null) {
            return false;
        }
        if (device.getName() == null) {
            if (debug) {
                LOG.log(Level.INFO, "AndroidBleService: Device with null name not allowed");
            }
            return false;
        }
        final boolean check = deviceNames.contains(device.getName());
        if (debug) {
            LOG.log(Level.INFO, "AndroidBleService: Device with name " + device.getName() + " in device list: " + check);
        }
        return check;
    }

    // native BLE Beacons
    private static native void startObserver(String[] uuids);
    private static native void stopObserver();
    private static native void startBroadcast(String uuid, int major, int minor, String id);
    private static native void stopBroadcast();

    // native BLE Devices
    private static native void startScanningPeripherals();
    private static native void stopScanningPeripherals();
    private static native void doConnect(String name, String address);
    private static native void doDisconnect(String name, String address);
    private static native void doRead(String address, String profile, String characteristic);
    private static native void doWrite(String address, String profile, String characteristic, byte[] value);
    private static native void doSubscribe(String address, String profile, String characteristic, boolean value);

    // callbacks BLE Beacons
    private static void setDetection(String uuid, int major, int minor, int rssi, int proximity) {
        ScanDetection detection = new ScanDetection();
        detection.setUuid(uuid);
        detection.setMajor(major);
        detection.setMinor(minor);
        detection.setRssi(rssi);
        detection.setProximity(proximity);
        Platform.runLater(() -> callback.accept(detection));
    }

    // callbacks BLE Devices
    private static void gotPeripheral(String name, String address) {
        if ((name != null && deviceNames.contains(name)) ||
                (name == null && address != null && deviceNames.contains(address))) {
            return;
        }
        if (name != null && address != null && deviceNames.contains(address)) {
            deviceNames.remove(address);
            devices.removeIf(d -> address.equals(d.getAddress()));
        }

        if (debug) {
            LOG.log(Level.INFO, String.format("AndroidBleService got peripheral named %s and address: %s", name, address));
        }
        BleDevice dev = new BleDevice();
        dev.setName(name);
        dev.setAddress(address);
        Platform.runLater(() -> devices.add(dev));
        deviceNames.add(name != null ? name : address);
    }

    private static void gotState(String name, String state) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE device %s changed state to %s", name, state));
        }

        getDeviceByName(name).ifPresent(device ->
                Platform.runLater(() -> device.setState(BleDevice.State.fromName(state))));
    }

    private static void gotProfile(String name, String uuid, String type) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE device has profile: %s with type: %s", uuid, type));
        }

        getDeviceByName(name).ifPresent(device -> {
            if (!profileNames.contains(uuid)) {
                profileNames.add(uuid);

                // if profile is not included yet:
                BleProfile bleProfile = new BleProfile();
                bleProfile.setUuid(UUID.fromString(uuid));
                bleProfile.setType(type);
                if (debug) {
                    LOG.log(Level.INFO, String.format("AndroidBleService creating profile %s", uuid));
                }
                Platform.runLater(() -> device.getProfiles().add(bleProfile));
            }
        });
    }

    private static void gotCharacteristic(String name, String profileUuid, String charUuid, String properties) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE profile %s has characteristic: %s with properties: %s", profileUuid, charUuid, properties));
        }

        getDeviceByName(name).ifPresent(device ->
                device.getProfiles().stream()
                        .filter(p -> p.getUuid().toString().equalsIgnoreCase(profileUuid))
                        .findAny()
                        .ifPresent(p -> {
                            if (debug) {
                                LOG.log(Level.INFO, String.format("AndroidBleService updating profile with characteristic %s", charUuid));
                            }
                            boolean exists = false;
                            for (BleCharacteristic c : p.getCharacteristics()) {
                                if (c.getUuid().toString().equalsIgnoreCase(charUuid)) {
                                    c.setProperties(properties);
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                BleCharacteristic bleCharacteristic = new BleCharacteristic(UUID.fromString(charUuid));
                                bleCharacteristic.setProperties(properties);
                                Platform.runLater(() -> p.getCharacteristics().add(bleCharacteristic));
                            }
                        }));
    }

    private static void gotDescriptor(String name, String profileUuid, String charUuid, String descUuid, byte[] value) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE profile %s has characteristic: %s with descriptor: %s and value %s", profileUuid, charUuid, descUuid, Arrays.toString(value)));
        }

        getDeviceByName(name).ifPresent(device ->
                device.getProfiles().stream()
                        .filter(p -> p.getUuid().toString().equalsIgnoreCase(profileUuid))
                        .findAny()
                        .ifPresent(p -> {
                            if (debug) {
                                LOG.log(Level.INFO, String.format("AndroidBleService updating profile with descriptor %s and value %s", descUuid, Arrays.toString(value)));
                            }
                            p.getCharacteristics().stream()
                                    .filter(c -> c.getUuid().toString().equalsIgnoreCase(charUuid))
                                    .findAny()
                                    .ifPresent(c -> c.getDescriptors().stream()
                                            .filter(d -> d.getUuid().toString().equalsIgnoreCase(descUuid))
                                            .findAny()
                                            .ifPresentOrElse(d -> d.setValue(value),
                                                    () -> {
                                                        BleDescriptor d = new BleDescriptor();
                                                        d.setUuid(UUID.fromString(descUuid));
                                                        d.setValue(value);
                                                        c.getDescriptors().add(d);
                                                    }));
                        }));
    }

    private static void gotValue(String name, String charUuid, byte[] value) {
        if (debug) {
            LOG.log(Level.INFO, String.format("BLE with characteristic: %s has value %s", charUuid, Arrays.toString(value)));
        }

        getDeviceByName(name).ifPresent(device ->
                device.getProfiles().stream()
                        .flatMap(d -> d.getCharacteristics().stream())
                        .filter(c -> c.getUuid().toString().equalsIgnoreCase(charUuid))
                        .findFirst()
                        .ifPresent(c -> {
                            if (debug) {
                                LOG.log(Level.INFO, String.format("AndroidBleService DONE updating value for characteristic %s", charUuid));
                            }
                            Platform.runLater(() -> c.setValue(value));
                        }));
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

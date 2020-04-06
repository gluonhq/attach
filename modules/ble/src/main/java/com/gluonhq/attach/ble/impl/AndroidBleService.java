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
import com.gluonhq.attach.ble.BleSpecs;
import com.gluonhq.attach.ble.Configuration;
import com.gluonhq.attach.ble.ScanDetection;
import com.gluonhq.attach.util.Constants;
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
 * Android implementation of BleService
 * 
*/
public class AndroidBleService implements BleService {

    private static final Logger LOG = Logger.getLogger(AndroidBleService.class.getName());
    private static final ObservableList<BleDevice> devices = FXCollections.observableArrayList();
    private static final List<String> deviceNames = new LinkedList<>();
    private static boolean debug;

    private static Consumer<ScanDetection> callback;

    static {
        LOG.fine("Loading AndroidBleService");
        debug = Boolean.getBoolean(Constants.ATTACH_DEBUG);
        System.loadLibrary("Ble");
        LOG.fine("Loaded AndroidBleService");
    }
    
    public AndroidBleService() {
        LOG.fine("Created AndroidBleService instance");
    }

    public void startScanning(Configuration region, Consumer<ScanDetection> callback) {
        LOG.fine("AndroidBleService will start scanning");
        AndroidBleService.callback = callback;
        String[] uuids = new String[region.getUuids().size()];
        uuids = region.getUuids().toArray(uuids);
        startObserver(uuids);
    }

    public void stopScanning() {
        LOG.fine("AndroidBleService will stop scanning");
        stopObserver();
    }

    public ObservableList<BleDevice> startScanningDevices() {
        LOG.fine("AndroidBleService will start scanning devices");
        devices.clear();
        startScanningPeripherals();
        return devices;
    }

    public void connect(BleDevice device) {
System.err.println("[ABLE]");
    }

    public void disconnect(BleDevice device) {
System.err.println("[ABLE]");
    }

    public void readCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
System.err.println("[ABLE]");
    }

    public void writeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic, byte[] value) {
System.err.println("[ABLE]");
    }

    public void subscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
System.err.println("[ABLE]");
    }

    public void unsubscribeCharacteristic(BleDevice device, UUID uuidProfile, UUID uuidCharacteristic) {
System.err.println("[ABLE]");
    }
    

    public void startBroadcasting(UUID beaconUUID, int major, int minor, String identifier) {
        startBroadcast(beaconUUID.toString(), major, minor, identifier);
    }

    /**
     * Stop advertising the current iOS device as a Bluetooth beacon
     *
     * @since 4.0.7
     */
    public void stopBroadcasting() {
        stopBroadcast();
    }

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
            LOG.log(Level.INFO, String.format("AndroidBleService got peripheral named %s and uuid: %s", name, uuid));
        }
        BleDevice dev = new BleDevice();
        dev.setName(name);
        dev.setAddress(uuid);
        Platform.runLater(() -> devices.add(dev));
        deviceNames.add(name != null ? name : uuid);

    }

    private static native void startScanningPeripherals();
    private static native void startObserver(String[] uuids);
    private static native void stopObserver();
    private static native void startBroadcast(String uuid, int major, int minor, String id);
    private static native void stopBroadcast();


}

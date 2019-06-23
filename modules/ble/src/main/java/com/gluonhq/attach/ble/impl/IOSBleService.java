/*
 * Copyright (c) 2016, 2019, Gluon
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

import com.gluonhq.attach.ble.BleService;
import com.gluonhq.attach.ble.Configuration;
import com.gluonhq.attach.ble.ScanDetection;
import javafx.application.Platform;

import java.util.function.Consumer;

/**
 * iOS implementation of BleService
 * 
 * Important note: it requires adding to the info.plist file:
 * 
 * {@code 
 *         <key>NSLocationUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 6+)</string>
 *         <key>NSLocationAlwaysUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+)</string>
 * }
*/
public class IOSBleService implements BleService {

    static {
        System.loadLibrary("Ble");
        initBle();
    }
    
    private static Consumer<ScanDetection> callback;
    
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
        this.callback = callback;
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
    
    
    // native
    private static native void initBle(); // init IDs for java callbacks from native
    private static native void startObserver(String[] uuids);
    private static native void stopObserver();
    
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
}

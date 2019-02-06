/*
 * Copyright (c) 2016, 2019 Gluon
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

import java.util.Optional;
import java.util.function.Consumer;

/**
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
 *  Services.get(BleService.class).ifPresent(service -> {
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
 * <p>The following keys are required:</p>
 * <pre>
 * {@code <key>NSLocationUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 6+)</string>
 *  <key>NSLocationAlwaysUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 8+)</string>}</pre>
 *
 * @since 3.0.0
 */
public interface BleService {

    /**
     * Returns an instance of {@link BleService}.
     * @return An instance of {@link BleService}.
     */
    static Optional<BleService> create() {
        return Services.get(BleService.class);
    }

    /**
     * Start scanning for BLE devices. When such a device is discovered, the callback will
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

}

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
package com.gluonhq.attach.barcodescan;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The barcode scanner can be used to scan barcodes of different types. The implementation of
 * the service will typically open the device's camera to scan for a barcode. When a valid
 * barcode could be detected and scanned, the string value that is represented by the barcode
 * will be returned as a result.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(BarcodeScanService.class).ifPresent(service -> {
 *      Optional<String> barcode = barcodeScanService.scan();
 *      barcode.ifPresent(barcodeValue -> System.out.println("Scanned Bar Code: " + barcodeValue));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permission <code>android.permission.CAMERA</code> needs to be added together
 * with the following <code>activity</code> configuration that handles the SCAN intent
 * of the BarcodeScanService.</p>
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.CAMERA"/>
 *    ...
 *    <application ...>
 *      ...
 *      <activity android:name="com.gluonhq.helloandroid.zxing.CaptureActivity"
 *                android:screenOrientation="sensorLandscape"
 *                android:clearTaskOnLaunch="true"
 *                android:stateNotNeeded="true"
 *                android:windowSoftInputMode="stateAlwaysHidden">
 *        <intent-filter>
 *          <action android:name="com.gluonhq.attach.barcodescan.android.SCAN"/>
 *          <category android:name="android.intent.category.DEFAULT"/>
 *        </intent-filter>
 *      </activity>
 *      <activity android:name="com.gluonhq.impl.attach.plugins.android.PermissionRequestActivity" />
 *    </application>
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 *
 * <p>The following keys are required:</p>
 * <pre>
 * {@code <key>NSCameraUsageDescription</key>
 *  <string>Reason to use Camera Service (iOS 10+)</string>}</pre>
 *
 * @since 3.0.0
 */
public interface BarcodeScanService {

    /**
     * Returns an instance of {@link BarcodeScanService}.
     * @return An instance of {@link BarcodeScanService}.
     */
    static Optional<BarcodeScanService> create() {
        return Services.get(BarcodeScanService.class);
    }

    /**
     * Starts up the scanner functionality (commonly provided via the camera), and then parsed by Attach to
     * determine the string the barcode represents.
     *
     * @return Returns an Optional containing the parsed string. The Optional may
     *         be empty if the String fails to be parsed for any reason, or if the
     *         user cancels the operation.
     */
    Optional<String> scan();

    /**
     * Starts up the scanner functionality (commonly provided via the camera),
     * and then parsed by Attach to determine the string the barcode represents.
     *
     * @param title The title of the scan view. If null or empty nothing will be
     *              displayed.
     * @param legend An explanatory message displayed in the scan view. If null or
     *               empty nothing will be displayed.
     * @param resultText The text to display when the scan ends successfully, before
     *                   the scanned text. If empty or null, the result won't be shown.
     *
     * @return Returns an Optional containing the parsed string. The Optional may
     *         be empty if the String fails to be parsed for any reason, or if the
     *         user cancels the operation.
     * @since 3.8.0
     */
    Optional<String> scan(String title, String legend, String resultText);

}

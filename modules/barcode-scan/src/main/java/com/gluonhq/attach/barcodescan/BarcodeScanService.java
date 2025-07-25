/*
 * Copyright (c) 2016, 2025, Gluon
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
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Optional;

/**
 * The barcode scanner can be used to scan barcodes of different types. The implementation of
 * the service will typically open the device's camera to scan for a barcode. When a valid
 * barcode could be detected and scanned, the string value that is represented by the barcode
 * will be returned as a result.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code BarcodeScanService.create().ifPresent(service -> {
 *      service.resultProperty().addListener((obs, ov, nv) ->
 *          System.out.printf("Scanned result: %s", nv.getResult()));
 *      service.asyncScan();
 *  });}</pre>
 *
 * <p><b>Requirements</b></p>
 *
 * <p>The service requires the following changes on Android and iOS.</p>
 *
 * <p>However, these are handled automatically by the <a href="https://docs.gluonhq.com/">GluonFX plugin</a>,
 * when used.</p>
 *
 * <p><b>Android Configuration</b></p>
 * <p>None</p>
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
     * @since 4.0.16
     */
    void asyncScan();

    /**
     * Starts up the scanner functionality (commonly provided via the camera), in a blocking way,
     * and then parsed by Attach to determine the string the barcode represents.
     *
     * @return Returns an Optional containing the parsed string. The Optional may
     *         be empty if the String fails to be parsed for any reason, or if the
     *         user cancels the operation.
     *
     * @deprecated This method has been deprecated in favour of {@link #asyncScan()}.
     */
    @Deprecated
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
     * @since 4.0.16
     */
    void asyncScan(String title, String legend, String resultText);

    /**
     * Starts up the scanner functionality (commonly provided via the camera), in a blocking way,
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
     *
     * @deprecated This method has been deprecated in favour of {@link #asyncScan(String, String, String)}.
     */
    @Deprecated
    Optional<String> scan(String title, String legend, String resultText);

    /**
     * A read-only property containing the result of the scan.
     *
     * @return a read-only object property containing a string with the barcode or QR code scan
     *
     * @since 4.0.16
     */
    ReadOnlyStringProperty resultProperty();

}

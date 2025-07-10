/*
 * Copyright (c) 2020, 2025, Gluon
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
import android.util.Log;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

public class DalvikBarcodeScanService {

    private static final String TAG = Util.TAG;

    private final Activity activity;

    public DalvikBarcodeScanService(Activity activity) {
        this.activity = activity;
    }

    private void scan(String title, String legend, String resultText) {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(activity, options);
        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    if (Util.isDebug()) {
                        Log.v(TAG, "Barcodescan succeeded with result: " + barcode.getRawValue() + " and format " + barcode.getFormat());
                    }
                    nativeBarcodeScanResult(barcode.getRawValue());
                })
                .addOnCanceledListener(() -> {
                    if (Util.isDebug()) {
                        Log.v(TAG, "Barcodescan was cancelled");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Barcodescan failed with error: " + getErrorMessage(e)));
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof MlKitException) {
            switch (((MlKitException) e).getErrorCode()) {
                case MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED:
                    return "Camera permission not granted";
                case MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE:
                    return "app name unavailable";
                default:
                    return e.getMessage();
            }
        } else {
            return e.getMessage();
        }
    }

    private native void nativeBarcodeScanResult(String result);

}
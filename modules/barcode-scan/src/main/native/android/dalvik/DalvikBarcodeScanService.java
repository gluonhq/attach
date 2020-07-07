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
import android.content.Intent;
import android.util.Log;
import com.gluonhq.helloandroid.zxing.Intents.Scan;

public class DalvikBarcodeScanService {

    private static final String TAG = Util.TAG;
    private static final int SCAN_CODE = 10002;
    public static final String SCAN_VIEW_TITLE = "ScanViewTitle";
    public static final String SCAN_VIEW_LEGEND = "ScanViewLegend";
    public static final String SCAN_VIEW_RESULT = "ScanViewResult";

    private final Activity activity;
    private final Intent scanIntent;

    public DalvikBarcodeScanService(Activity activity) {
        this.activity = activity;
        scanIntent = new Intent(Scan.ACTION);
        scanIntent.addCategory(Intent.CATEGORY_DEFAULT);
        scanIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        scanIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }

    private void scan(String title, String legend, String resultText) {
        if (title != null && !title.isEmpty()) {
            scanIntent.putExtra(SCAN_VIEW_TITLE, title);
        }

        if (legend != null && !legend.isEmpty()) {
            scanIntent.putExtra(SCAN_VIEW_LEGEND, legend);
        }

        if (resultText != null && !resultText.isEmpty()) {
            scanIntent.putExtra(SCAN_VIEW_RESULT, resultText);
        }

        if (!Util.verifyPermissions(Manifest.permission.CAMERA)) {
            Log.e(TAG, "Camera is disabled");
            return;
        }

        IntentHandler intentHandler = new IntentHandler() {
            @Override
            public void gotActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode == SCAN_CODE && resultCode == Activity.RESULT_OK) {
                    String result = (String) intent.getExtras().get("SCAN_RESULT");
                    // a barcode was scanned
                    if (Util.isDebug()) {
                        Log.v(TAG, "Current barcodescan result: " + result);
                    }
                    nativeBarcodeScanResult(result);
                }
            }
        };

        if (activity == null) {
            Log.e(TAG, "Activity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        Util.setOnActivityResultHandler(intentHandler);

        activity.startActivityForResult(scanIntent, SCAN_CODE);
    }

    private native void nativeBarcodeScanResult(String result);

}
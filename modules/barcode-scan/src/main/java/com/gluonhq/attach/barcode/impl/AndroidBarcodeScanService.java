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
package com.gluonhq.attach.barcode.impl;

import com.gluonhq.attach.barcode.BarcodeScanService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

/**
 * Requires this permission: android.permission.CAMERA
 *
 * And this activity added to the AndroidManifest.xml:
 *
 * <pre>
 * {@code
 *  <activity android:name="com.gluonhq.helloandroid.zxing.CaptureActivity"
 *        android:screenOrientation="sensorLandscape"
 *        android:clearTaskOnLaunch="true"
 *       android:stateNotNeeded="true"
 *        android:windowSoftInputMode="stateAlwaysHidden">
 *         <intent-filter>
 *             <action android:name="com.gluonhq.attach.barcode.android.SCAN"/>
 *            <category android:name="android.intent.category.DEFAULT"/>
 *          </intent-filter>
 *    </activity>
 *    <activity android:name="com.gluonhq.helloandroid.PermissionRequestActivity" />
 *    }
 * </pre>
 */
public class AndroidBarcodeScanService implements BarcodeScanService {
    
    static {
        System.loadLibrary("barcodescan");
    }
    
    private static StringProperty result;
        
    @Override
    public Optional<String> scan() {
        return scan("", "", "");
    }
    
    @Override
    public Optional<String> scan(String title, String legend, String resultText) {
        result = new SimpleStringProperty();
        startBarcodeScan(title != null ? title : "", legend != null ? legend : "", resultText != null ? resultText : "");
        try {
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            System.out.println("ScanActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    // native
    private static native void startBarcodeScan(String title, String legend, String resultText);

    // callback
    public static void setResult(String v) {
        result.set(v);
        Platform.runLater(() -> {
            try {
                Platform.exitNestedEventLoop(result, null);
            } catch (Exception e) {
                System.out.println("ScanActivity: exitNestedEventLoop failed: " + e);
            }
        });
    }
}

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
package com.gluonhq.attach.barcodescan.impl;

import com.gluonhq.attach.barcodescan.BarcodeScanService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.util.Optional;

public class AndroidBarcodeScanService implements BarcodeScanService {
    
    static {
        System.loadLibrary("barcodescan");
    }

    private static final ReadOnlyStringWrapper scanResult = new ReadOnlyStringWrapper();
    private static StringProperty result;
    private static boolean enteredLoop;

    @Override
    public Optional<String> scan() {
        return scan("", "", "");
    }
    
    @Override
    public Optional<String> scan(String title, String legend, String resultText) {
        result = new SimpleStringProperty();
        startBarcodeScan(title != null ? title : "", legend != null ? legend : "", resultText != null ? resultText : "");
        try {
            enteredLoop = true;
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            System.out.println("ScanActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    @Override
    public void asyncScan() {
        asyncScan("", "", "");
    }
    
    @Override
    public void asyncScan(String title, String legend, String resultText) {
        scanResult.set(null);
        startBarcodeScan(title != null ? title : "", legend != null ? legend : "", resultText != null ? resultText : "");
    }

    @Override
    public ReadOnlyStringProperty resultProperty() {
        return scanResult.getReadOnlyProperty();
    }

    // native
    private static native void startBarcodeScan(String title, String legend, String resultText);

    // callback
    public static void setResult(String v) {
        if (enteredLoop) {
            enteredLoop = false;
            result.set(v);
            Platform.runLater(() -> {
                try {
                    Platform.exitNestedEventLoop(result, null);
                } catch (Exception e) {
                    System.out.println("ScanActivity: exitNestedEventLoop failed: " + e);
                }
            });
            return;
        }
        Platform.runLater(() -> scanResult.set(v));
    }
}

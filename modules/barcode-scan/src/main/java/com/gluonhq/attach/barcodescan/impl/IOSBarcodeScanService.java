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
package com.gluonhq.attach.barcodescan.impl;

import com.gluonhq.attach.barcodescan.BarcodeScanService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

/**
 * Note: Since iOS 10, the key {@code NSCameraUsageDescription} is required in 
 * the plist file in order to use this service
 */
public class IOSBarcodeScanService implements BarcodeScanService {
    
    static {
        System.loadLibrary("BarcodeScan");
        initBarcodeScan();
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
    
    // native
    
    private static native void initBarcodeScan(); // init IDs for java callbacks from native

    // scanning service
    private static native void startBarcodeScan(String title, String legend, String resultText);
   
}

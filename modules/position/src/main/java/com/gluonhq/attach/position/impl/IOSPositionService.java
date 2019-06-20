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
package com.gluonhq.attach.position.impl;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.util.Constants;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * An implementation of the
 * {@link PositionService PositionService} for the
 * iOS platform.
 * 
 * Important note: it requires adding to the info.plist file:
 * 
 * {@code 
 *         <key>NSLocationUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 6+)</string>
 *         <key>NSLocationWhenInUseUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+)</string>
 * }
 * 
 * With Background mode enabled
 * {@code 
 *         <key>UIBackgroundModes</key>
 *         <array>
 *             <string>location</string>
 *         </array> 
 *         <key>NSLocationUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 6+)</string>
 *         <key>NSLocationAlwaysUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+) in background</string>
 *         <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+) in background</string>
 * }
 */
public class IOSPositionService implements PositionService {

    static {
        System.loadLibrary("Position");
        initPosition();
    }

    private static ReadOnlyObjectWrapper<Position> position;
    private static boolean running;
    private Parameters parameters;
    
    public IOSPositionService() {
        if ("true".equals(System.getProperty(Constants.ATTACH_DEBUG))) {
            enableDebug();
        }
        position = new ReadOnlyObjectWrapper<>();

        LifecycleService.create().ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, () -> {
                if (! parameters.isBackgroundModeEnabled()) {
                    stopObserver();
                }
            });
            l.addListener(LifecycleEvent.RESUME, () -> {
                if (! parameters.isBackgroundModeEnabled()) {
                    startObserver(parameters.getAccuracy().name(), parameters.getTimeInterval(), 
                            parameters.getDistanceFilter(), parameters.isBackgroundModeEnabled());
                }
            });
        });
    }

    @Override
    public void start() {
        start(DEFAULT_PARAMETERS);
    }

    @Override
    public void start(Parameters parameters) {
        if (running) {
            stop();
        }
        this.parameters = parameters;
        startObserver(parameters.getAccuracy().name(), parameters.getTimeInterval(), 
                parameters.getDistanceFilter(), parameters.isBackgroundModeEnabled());
        running  = true;
    }

    @Override
    public void stop() {
        stopObserver();
        running = false;
    }

    @Override
    public ReadOnlyObjectProperty<Position> positionProperty() {
        return position.getReadOnlyProperty();
    }

    @Override
    public Position getPosition() {
        return positionProperty().get();
    }
    
    // native
    private static native void initPosition(); // init IDs for java callbacks from native
    private static native void startObserver(String accuracy, long timeInterval, float distanceFilter, boolean backgroundModeEnabled);
    private static native void stopObserver();
    private static native void enableDebug();
    
    // callback
    private static void setLocation(double lat, double lon, double alt) {
        Position p = new Position(lat, lon, alt);
        Platform.runLater(() -> position.set(p));
    }

}

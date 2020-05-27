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
package com.gluonhq.attach.position.impl;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.position.impl.geotools.EarthGravitationalModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the
 * {@link PositionService PositionService} for the
 * Android platform.
 * 
 */
public class AndroidPositionService implements PositionService {

    private static Logger LOG = Logger.getLogger(AndroidPositionService.class.getName());

    static {
        System.loadLibrary("Position");
    }

    private static ReadOnlyObjectWrapper<Position> position;
    private static boolean running;
    private Parameters parameters = DEFAULT_PARAMETERS;
    private static EarthGravitationalModel gh;

    public AndroidPositionService() {
        position = new ReadOnlyObjectWrapper<>();

        LifecycleService.create().ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, () -> {
                if (! parameters.isBackgroundModeEnabled()) {
                    stopObserver();
                }
            });
            l.addListener(LifecycleEvent.RESUME, () -> {
                if (! parameters.isBackgroundModeEnabled()) {
                    startObserver(parameters.getTimeInterval(),
                            parameters.getDistanceFilter(), parameters.isBackgroundModeEnabled());
                }
            });
        });

        gh = new EarthGravitationalModel();
        try {
            gh.load("/egm180.nor");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to load nor file", e);
            e.printStackTrace();
        }
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
        startObserver(parameters.getTimeInterval(), parameters.getDistanceFilter(),
                parameters.isBackgroundModeEnabled());
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

    private static native void startObserver(long timeInterval, float distanceFilter, boolean backgroundModeEnabled);
    private static native void stopObserver();

    // callback
    private static void setLocation(double lat, double lon, double alt) {
        double altitudeMeanSeaLevel = alt;
        if (alt != 0.0) {
            try {
                double offset = gh.heightOffset(lon, lat, alt);
                altitudeMeanSeaLevel = alt - offset;
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error getting altitude mean sea level", ex);
            }
        }
        Position p = new Position(lat, lon, altitudeMeanSeaLevel);
        Platform.runLater(() -> position.set(p));
    }

}

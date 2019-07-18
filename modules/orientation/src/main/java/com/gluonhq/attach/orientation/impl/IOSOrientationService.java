/*
 * Copyright (c) 2016, Gluon
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
package com.gluonhq.attach.orientation.impl;


import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.attach.util.impl.Utils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Orientation;

import java.util.Optional;

public class IOSOrientationService implements OrientationService {

    static {
        System.loadLibrary("Orientation");
        initOrientation();
    }
    
    private static ReadOnlyObjectWrapper<Orientation> orientation;
    private static String orientationText = "Unknown";

    public IOSOrientationService() {
        orientation = new ReadOnlyObjectWrapper<>();
        Utils.runOnAppThread(this::setupObserver);
    }

    private void setupObserver() {
        LifecycleService.create().ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, IOSOrientationService::stopObserver);
            l.addListener(LifecycleEvent.RESUME, IOSOrientationService::startObserver);
        });
        startObserver();
    }

    @Override
    public ReadOnlyObjectProperty<Orientation> orientationProperty() {
        return orientation.getReadOnlyProperty();
    }
    
    @Override
    public final Optional<Orientation> getOrientation() {
        return convertOrientation(orientationText);
    }
    
    // native
    private static native void initOrientation();
    private static native void startObserver();
    private static native void stopObserver();
    
    // callback
    private static void notifyOrientation(String o) {
        convertOrientation(o).ifPresent(or -> {
            if (orientation.get() == null || !orientation.get().equals(or)) {
                Platform.runLater(() -> orientation.setValue(or));
            }
        });
    }

    private static Optional<Orientation> convertOrientation(String text) {
        orientationText = text;
        switch (orientationText) {
            case "Portrait":
            case "PortraitUpsideDown":
                return Optional.of(Orientation.VERTICAL);
            case "LandscapeLeft":
            case "LandscapeRight":
                return Optional.of(Orientation.HORIZONTAL);
            case "Unknown":
            default:
                return Optional.empty();
        }
    }
    
}
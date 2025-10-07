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
package com.gluonhq.attach.display.impl;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;

public class IOSDisplayService implements DisplayService {

    static {
        System.loadLibrary("Display");
        initDisplay();
    }

    private static ReadOnlyObjectWrapper<DisplayService.Notch> notch;

    private static final ReadOnlyObjectWrapper<Insets> insetsProperty = new ReadOnlyObjectWrapper<>();

    public IOSDisplayService() {
        notch = new ReadOnlyObjectWrapper<>(Notch.UNKNOWN);
        LifecycleService.create().ifPresent(l -> {
                l.addListener(LifecycleEvent.PAUSE, IOSDisplayService::stopObserver);
                l.addListener(LifecycleEvent.RESUME, IOSDisplayService::startObserver);
            });
        startObserver();
    }

    @Override
    public boolean isPhone() {
        return isIphone();
    }

    @Override
    public boolean isTablet() {
        return !isIphone();
    }

    @Override
    public boolean isDesktop() {
        return false;
    }

    @Override
    public Dimension2D getScreenResolution() {
        double[] dim = screenSize();
        return new Dimension2D(dim[0], dim[1]);
    }

    @Override
    public Dimension2D getDefaultDimensions() {
        double[] dim = screenBounds();
        return new Dimension2D(dim[0], dim[1]);
    }

    @Override
    public float getScreenScale() {
        return screenScale();
    }

    @Override
    public boolean isScreenRound() {
        return false;
    }

    @Override
    public boolean hasNotch() {
        return isNotchFound();
    }

    @Override
    public ReadOnlyObjectProperty<Notch> notchProperty() {
        return notch.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Insets> systemBarsInsetsProperty() {
        return insetsProperty.getReadOnlyProperty();
    }

    // native
    private static native void initDisplay();

    private native static boolean isIphone();
    private native static double[] screenSize();
    private native static double[] screenBounds();
    private native static float screenScale();
    private native static boolean isNotchFound();

    private static native void startObserver();
    private static native void stopObserver();

    // callback
    private static void notifyDisplay(String o) {
        DisplayService.Notch d = DisplayService.Notch.valueOf(o);
        if (! notch.get().equals(d)) {
            Platform.runLater(() -> notch.setValue(d));
        }
    }
}
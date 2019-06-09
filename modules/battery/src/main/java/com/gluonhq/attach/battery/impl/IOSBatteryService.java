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
package com.gluonhq.attach.battery.impl;

import com.gluonhq.attach.battery.BatteryService;
import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;

public class IOSBatteryService implements BatteryService {

    static {
        System.loadLibrary("Battery");
        initBattery();
    }
     
    private static final ReadOnlyBooleanWrapper PLUGGED_IN = new ReadOnlyBooleanWrapper();
    private static final ReadOnlyFloatWrapper BATTERY_LEVEL = new ReadOnlyFloatWrapper();

    public IOSBatteryService() {
        LifecycleService.create().ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, IOSBatteryService::stopObserver);
            l.addListener(LifecycleEvent.RESUME, IOSBatteryService::startObserver);
        });
        startObserver();
    }
    
    @Override
    public float getBatteryLevel() {
        return BATTERY_LEVEL.get();
    }

    @Override
    public ReadOnlyFloatProperty batteryLevelProperty() {
        return BATTERY_LEVEL.getReadOnlyProperty();
    }

    @Override
    public boolean isPluggedIn() {
        return PLUGGED_IN.get();
    }

    @Override
    public ReadOnlyBooleanProperty pluggedInProperty() {
        return PLUGGED_IN.getReadOnlyProperty();
    }
    
    // native
    private static native void initBattery();
    private static native void startObserver();
    private static native void stopObserver();
    
    // callback
    private void notifyBatteryState(String state) {
        if (state == null) {
            return;
        }
        // ios docs: charging -> device is plugged into power and the battery is less than 100% charged
        // or full -> device is plugged into power and the battery is 100% charged
        boolean plugged = state.equals("Charging") || state.equals("Full");
        if (PLUGGED_IN != null && PLUGGED_IN.get() != plugged) {
            Platform.runLater(() -> PLUGGED_IN.set(plugged));
        }
    }
    private void notifyBatteryLevel(float level) {
        if (BATTERY_LEVEL != null && BATTERY_LEVEL.get() != level) {
            Platform.runLater(() -> BATTERY_LEVEL.set(level));
        }
    }
}
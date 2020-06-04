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
package com.gluonhq.attach.battery.impl;

import com.gluonhq.attach.battery.BatteryService;
import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;

public class AndroidBatteryService implements BatteryService {

    private static final ReadOnlyBooleanWrapper PLUGGED_IN = new ReadOnlyBooleanWrapper();
    private static final ReadOnlyFloatWrapper BATTERY_LEVEL = new ReadOnlyFloatWrapper(-1f);

    static {
        System.loadLibrary("battery");
    }

    public AndroidBatteryService() {
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
    
    // callback
    private static void notifyBattery(float level, boolean plugged) {
        if (PLUGGED_IN.get() != plugged) {
            Platform.runLater(() -> PLUGGED_IN.set(plugged));
        }
        if (BATTERY_LEVEL.get() != level) {
            Platform.runLater(() -> BATTERY_LEVEL.set(level));
        }
    }
}
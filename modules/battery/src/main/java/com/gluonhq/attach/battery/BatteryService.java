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
package com.gluonhq.attach.battery;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyFloatProperty;

import java.util.Optional;

/**
 * With the battery service, you can query the current battery level of the underlying
 * device and whether the device is currently plugged in to an external power source or not.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code BatteryService.create().ifPresent(service -> {
 *      float batteryLevel = service.getBatteryLevel();
 *      boolean pluggedIn = service.isPluggedIn();
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface BatteryService {

    /**
     * Returns an instance of {@link BatteryService}.
     * @return An instance of {@link BatteryService}.
     */
    static Optional<BatteryService> create() {
        return Services.get(BatteryService.class);
    }

    /**
     * Returns the current battery level of the device as a float between 0 and 1 (inclusive),
     * where 0 indicates the battery is completely empty, and 1 indicates that the battery
     * is fully charged.
     *
     * @return A float value between 0 and 1 to indicate battery charge levels.
     */
    float getBatteryLevel();

    /**
     * Returns a property that will be frequently updated to reflect the current battery level.
     * @return A property containing the current battery level.
     */
    ReadOnlyFloatProperty batteryLevelProperty();

    /**
     * A boolean representing whether the device is currently receiving power from an external
     * source (i.e. it is not running on battery power).
     * @return Returns true to indicate power is coming from an external source, and false to
     *      indicate that the battery is powering the device.
     */
    boolean isPluggedIn();

    /**
     * Returns a property that will be updated to reflect whether the device is plugged in to an
     * external power source or not.
     * @return A property containing the plugged-in status of the device.
     */
    ReadOnlyBooleanProperty pluggedInProperty();
}

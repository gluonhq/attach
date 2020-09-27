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
package com.gluonhq.attach.accelerometer;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.Optional;

/**
 * An accelerometer measures the acceleration force that is applied to a device in
 * all three physical axes (x, y and z).
 *
 * <p>The AccelerometerService provides a read-only {@link #accelerationProperty() acceleration property}
 * that is updated at regular intervals by the underlying platform implementation. A user of the
 * AccelerometerService can listen to changes of the acceleration force, by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #accelerationProperty() acceleration property}.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code AccelerometerService.create().ifPresent(service -> {
 *      Acceleration acceleration = service.getCurrentAcceleration();
 *      System.out.printf("Current acceleration: %.2f, %.2f, %.2f",
 *              acceleration.getX(), acceleration.getY(), acceleration.getZ());
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface AccelerometerService {

    // TODO: Provide API to modify these settings:
    boolean FILTER_GRAVITY = false;
    
    int FREQUENCY = 50; // in Hz

    /**
     * Returns an instance of {@link AccelerometerService}.
     * @return An instance of {@link AccelerometerService}.
     */
    static Optional<AccelerometerService> create() {
        return Services.get(AccelerometerService.class);
    }
    
    /**
     * Returns a single reading from the accelerometer.
     *
     * @return the current accelerometer reading
     */
    Acceleration getCurrentAcceleration();

    /**
     * A frequently-updated reading from the accelerometer.
     *
     * @return A property containing a frequently-updated accelerometer reading.
     */
    ReadOnlyObjectProperty<Acceleration> accelerationProperty();

    // TODO: Add Gyroscope
}

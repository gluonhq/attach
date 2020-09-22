/*
 * Copyright (c) 2016, 2019 Gluon
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
package com.gluonhq.attach.magnetometer;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.Optional;

/**
 * A magnetometer measures the ambient geomagnetic field for all three physical axes (x, y and z).
 *
 * <p>The service gets started by calling either {@link #start() } or
 * {@link #start(Parameters) }, and can be stopped
 * at any time by calling {@link #stop() }.</p>
 *
 * <p>The MagnetometerService provides a read-only {@link #readingProperty() reading property}
 * that is updated at regular intervals by the underlying platform implementation. A user of the
 * MagnetometerService can listen to changes of the magnetic field, by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #readingProperty() reading property}.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(MagnetometerService.class).ifPresent(service -> {
 *      service.start();
 *      MagnetometerReading reading = service.getReading();
 *      System.out.printf("Magnetic field: %.4f, %.4f, %.4f. Magnitude: %.4f",
 *              reading.getX(), reading.getY(), reading.getZ(), reading.getMagnitude());
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface MagnetometerService {

    /**
     * Default frequency is 10 Hz (10 samples per second).
     */
    Parameters DEFAULT_PARAMETERS = new Parameters(10);

    /**
     * Returns an instance of {@link MagnetometerService}.
     * @return An instance of {@link MagnetometerService}.
     */
    static Optional<MagnetometerService> create() {
        return Services.get(MagnetometerService.class);
    }

    /**
     * Returns a single reading from the magnetometer.
     *
     * @return the current magnetometer reading
     */
    MagnetometerReading getReading();

    /**
     * A frequently-updated reading from the magnetometer.
     *
     * @return A property containing a frequently-updated magnetometer reading.
     */
    ReadOnlyObjectProperty<MagnetometerReading> readingProperty();

    /**
     * Starts the service with {@link #DEFAULT_PARAMETERS}.
     *
     * @since 4.0.10
     */
    void start();

    /**
     * Starts the service with given parameters.
     *
     * @param parameters Parameters for configuring the service
     * @since 4.0.10
     */
    void start(Parameters parameters);

    /**
     * Stops the service.
     *
     * @since 4.0.10
     */
    void stop();
}

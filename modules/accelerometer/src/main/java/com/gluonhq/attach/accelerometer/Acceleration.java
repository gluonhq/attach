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

import java.time.LocalDateTime;

/**
 * Represents a reading taken from the {@link AccelerometerService}.
 *
 * By using a device's accelerometer the acceleration force in three physical axes can be determined.
 *
 * @see AccelerometerService
 * @since 3.0.0
 */
public class Acceleration {

    private final double x;
    private final double y;
    private final double z;
    private final LocalDateTime timestamp;

    /**
     * Creates a new immutable Acceleration instance with the given x, y, z, and timestamp values.
     * @param x The x value of the reading
     * @param y The y value of the reading
     * @param z The z value of the reading
     * @param timestamp The instant at which the reading took place
     */
    public Acceleration(double x, double y, double z, LocalDateTime timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    /**
     * Returns the x value of the reading.
     * @return The x value of the reading.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y value of the reading
     * @return The y value of the reading.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the z value of the reading
     * @return The z value of the reading.
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns the timestamp of the reading
     * @return The timestamp of the reading.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

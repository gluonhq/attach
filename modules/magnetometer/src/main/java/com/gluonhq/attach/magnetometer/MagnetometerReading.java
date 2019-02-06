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

/**
 * Represents a reading taken from the {@link MagnetometerService}.
 * 
 * By using a device's geomagnetic field sensor in combination with the device's accelerometer, 
 * three orientation angles (azimut, pitch and roll) can be determined
 *
 * @see MagnetometerService
 * @since 3.0.0
 */
public final class MagnetometerReading {

    private final double x;
    private final double y;
    private final double z;
    private final double magnitude;
    private final double yaw;
    private final double pitch;
    private final double roll;
    private double azimuth;

    /**
     * Creates a new immutable Reading instance with the given x, y, z, magnitude values, and orientation angles.
     * @param x The x value of the reading
     * @param y The y value of the reading
     * @param z The z value of the reading
     * @param magnitude The magnitude of the reading
     * @param yaw rotation about the -z axis
     * @param pitch rotation about the x axis
     * @param roll rotation about the y axis
     */
    public MagnetometerReading(double x, double y, double z, double magnitude, double yaw, double pitch, double roll) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = magnitude;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.azimuth = Math.toDegrees(yaw);
        if (azimuth < 0) {
            azimuth += 360;
        }
    }

    /**
     * Returns the x value of the reading, microtesla units.
     * @return The x value of the reading.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y value of the reading, microtesla units
     * @return The y value of the reading.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the z value of the reading, microtesla units
     * @return The z value of the reading.
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns the magnitude of the reading, microtesla units
     * @return The magnitude of the reading.
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * Yaw (rotation about the -z axis, radians). 
     * This is the angle between the device's current compass direction and the magnetic north. 
     * Top edge of the device faces magnetic N: 0 degrees, S: Pi, E: Pi/2, W: -Pi/2.
     * The range of values is -Pi to Pi.
     * @return yaw angle
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * Pitch (rotation about the x axis, radians). 
     * This is the angle between a plane parallel to the device's screen and a plane parallel to the ground. 
     * Tilting the top edge of the device toward the ground: the pitch angle becomes positive. 
     * Tilting the top edge of the device away from the ground: the pitch angle to become negative. 
     * The range of values is -Pi to Pi.
     * @return pitch angle
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * Roll (rotation about the y axis, radians). 
     * This is the angle between a plane perpendicular to the device's screen and a plane perpendicular 
     * to the ground. 
     * Tilting to the left the device toward the ground, the roll angle becomes positive. 
     * Tilting to the right the device toward the ground, the roll angle to become negative. 
     * The range of values is -Pi to Pi.
     * @return roll angle
     */
    public double getRoll() {
        return roll;
    }

    /**
     * Azimuth (rotation about the -z axis, degrees). 
     * Returns the compass reading, represented as a value between 0 and 359.99, 
     * where 0 is the magentic north.
     *
     * @return The current compass reading in degrees.
     */
    public double getAzimuth() {
        return azimuth;
    }

}

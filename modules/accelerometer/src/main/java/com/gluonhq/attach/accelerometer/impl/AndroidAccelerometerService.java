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
package com.gluonhq.attach.accelerometer.impl;

import com.gluonhq.attach.accelerometer.Acceleration;
import com.gluonhq.attach.accelerometer.AccelerometerService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AndroidAccelerometerService implements AccelerometerService {

    static {
        System.loadLibrary("accelerometer");
    }

    private static ReadOnlyObjectWrapper<Acceleration> acceleration;

    public AndroidAccelerometerService() {
        acceleration = new ReadOnlyObjectWrapper<>();
        initAccelerometer(FILTER_GRAVITY, FREQUENCY);
    }    
    
    @Override
    public Acceleration getCurrentAcceleration() {
        return acceleration.get();
    }

    @Override
    public ReadOnlyObjectProperty<Acceleration> accelerationProperty() {
        return acceleration.getReadOnlyProperty();
    }
    
    // native
    private static native void initAccelerometer(boolean filterGravity, int rateInMillis);

    // callback
    private static void notifyAcceleration(double x, double y, double z, double t) {
        Acceleration a = new Acceleration(x, y, z, toLocalDateTime(t));
        Platform.runLater(() -> acceleration.setValue(a));
    }
    
    private static LocalDateTime toLocalDateTime(double t) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) t), ZoneId.systemDefault());
    }
}

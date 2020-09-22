/*
 * Copyright (c) 2020, Gluon
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
package com.gluonhq.attach.magnetometer.impl;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.magnetometer.MagnetometerReading;
import com.gluonhq.attach.magnetometer.MagnetometerService;
import com.gluonhq.attach.magnetometer.Parameters;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public abstract class MobileMagnetometerService implements MagnetometerService {

    protected static final ReadOnlyObjectWrapper<MagnetometerReading> reading = new ReadOnlyObjectWrapper<>();
    private static boolean isRunning = false;
    private Parameters parameters = DEFAULT_PARAMETERS;

    public MobileMagnetometerService() {
        LifecycleService.create().ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, () -> stopMagnetometerImpl());
            l.addListener(LifecycleEvent.RESUME, () -> {
                // isRunning keeps track of the service start-stop lifecycle
                // hence, if the user did not call stop, then isRunning = true
                // therefore, on resume, we should "restart" the service, which we stopped on pause
                if (isRunning) {
                    start(parameters);
                }
            });
        });
    }

    @Override
    public ReadOnlyObjectProperty<MagnetometerReading> readingProperty() {
        return reading.getReadOnlyProperty();
    }

    @Override
    public MagnetometerReading getReading() {
        return reading.get();
    }

    @Override
    public void start() {
        start(DEFAULT_PARAMETERS);
    }

    @Override
    public void start(Parameters parameters) {
        if (isRunning) {
            stopMagnetometerImpl();
        }

        this.parameters = parameters;
        startMagnetometerImpl(parameters.getFrequency());
        isRunning = true;
    }

    @Override
    public void stop() {
        stopMagnetometerImpl();
        isRunning = false;
    }

    protected abstract void startMagnetometerImpl(double frequency);
    protected abstract void stopMagnetometerImpl();
}

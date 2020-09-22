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
package com.gluonhq.attach.magnetometer.impl;

import com.gluonhq.attach.magnetometer.MagnetometerReading;
import javafx.application.Platform;

public class IOSMagnetometerService extends MobileMagnetometerService {

    static {
        System.loadLibrary("Magnetometer");
        initMagnetometer();
    }

    @Override
    protected void startMagnetometerImpl(double frequency) {
        startObserver(frequency);
    }

    @Override
    protected void stopMagnetometerImpl() {
        stopObserver();
    }

    // native
    private static native void initMagnetometer();
    private static native void startObserver(double frequency);
    private static native void stopObserver();
    
    // callback
    private static void notifyReading(double x, double y, double z, double m, double a, double p, double r) {
        MagnetometerReading read = new MagnetometerReading(x, y, z, m, a, p, r);
        Platform.runLater(() -> reading.setValue(read));
    }
}

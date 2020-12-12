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
package com.gluonhq.attach.compass.impl;

import com.gluonhq.attach.compass.CompassService;
import com.gluonhq.attach.magnetometer.MagnetometerService;
import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public abstract class MobileCompassService implements CompassService {

    private final ReadOnlyDoubleWrapper heading = new ReadOnlyDoubleWrapper();

    public MobileCompassService() {
        Services.get(MagnetometerService.class).ifPresent(m -> {
            m.readingProperty().addListener((obs, ov, nv) -> heading.setValue(nv.getAzimuth()));
        });
    }

    @Override
    public double getHeading() {
        return heading.get();
    }

    @Override
    public ReadOnlyDoubleProperty headingProperty() {
        return heading.getReadOnlyProperty();
    }

    @Override
    public void start() {
        Services.get(MagnetometerService.class).ifPresent(MagnetometerService::start);
    }

    @Override
    public void stop() {
        Services.get(MagnetometerService.class).ifPresent(MagnetometerService::stop);
    }
}

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
package com.gluonhq.attach.compass;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyDoubleProperty;

import java.util.Optional;

/**
 * The compass service provides a compass heading in degrees as a value between 0.0 and 359.99,
 * where a value of 0.0 represents north.
 *
 * <p>The CompassService provides a read-only {@link #headingProperty() heading property}
 * that is updated at regular intervals by the underlying platform implementation. A user of the
 * CompassService can listen to changes of the heading, by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #headingProperty() heading property}.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code CompassService.create().ifPresent(service -> {
 *      double heading = service.getHeading();
 *      System.out.printf("Current heading: %.2f", heading);
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface CompassService {

    /**
     * Returns an instance of {@link CompassService}.
     * @return An instance of {@link CompassService}.
     */
    static Optional<CompassService> create() {
        return Services.get(CompassService.class);
    }

    /**
     * Returns the compass heading, represented as a value between 0.0 and 359.99, where 0.0 is north.
     * If there is no heading data available, -1.0 will be returned to represent this error state.
     *
     * @return The current compass heading.
     */
    double getHeading();

    /**
     * A frequently-updated heading of the compass.
     *
     * @return A property containing a frequently-updated compass heading.
     */
    ReadOnlyDoubleProperty headingProperty();
}

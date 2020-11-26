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
package com.gluonhq.attach.display;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Dimension2D;

import java.util.Optional;

/**
 * With the display service you can query the characteristics of your device's screen. It can
 * return the screen resolution and provides a few utility methods to see if the display is a
 * regular phone, a tablet or a desktop.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code DisplayService.create().ifPresent(service -> {
 * 　　  Dimension2D resolution = service.getScreenResolution();
 *      System.out.printf("Screen resolution: %.0fx%.0f", resolution.getWidth(), resolution.getHeight());
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface DisplayService {

    /**
     * Possible Notch positions
     * @since 3.8.0
     */
    public enum Notch {
        /**
         * The notch is located at the top of the screen, with the device held 
         * upright and in portrait mode
         */
        TOP,

        /**
         * The notch is located at the bottom of the screen, with the device held
         * upright and in portrait mode but upside down
         */
        BOTTOM,

        /**
         * The notch is located at the left of the screen, with the device held 
         * upright and in landscape mode
         */
        LEFT,

        /**
         * The notch is located at the right of the screen, with the device held 
         * upright and in landscape mode
         */
        RIGHT,

        /**
         * There is no notch present or its location is unknown
         */
        UNKNOWN
    };

    /**
     * Returns an instance of {@link DisplayService}.
     * @return An instance of {@link DisplayService}.
     */
    static Optional<DisplayService> create() {
        return Services.get(DisplayService.class);
    }

    /**
     * Returns true if the device is considered a phone.
     * @return True if the device is considered a phone.
     */
    boolean isPhone();

    /**
     * Returns true if the device is considered a tablet.
     * @return True if the device is considered a tablet.
     *
     * Running on Desktop, if the property {@code charm-desktop-form}
     * is set to {@code tablet}, it will return true. False otherwise
     */
    boolean isTablet();

    /**
     * Returns true if the device is considered a desktop / laptop.
     * @return True if the device is considered a desktop / laptop.
     */
    boolean isDesktop();

    /**
     * Returns the screen resolution of the device, in pixels.
     * @return The screen resolution of the device.
     */
    Dimension2D getScreenResolution();

    /**
     * Returns the default screen dimensions of a mobile device, in density 
     * independent pixel units. 
     * In case of desktop, it gives the form factor of either a phone or a tablet.
     * @return The screen dimensions of a mobile device, in dp units.
     * @since 3.7.0
     */
    Dimension2D getDefaultDimensions();

    /**
     * Returns the logical density of the display
     * @return the screen scale
     * @since 3.3.0
     */
    float getScreenScale();

    /**
     * Returns true if the device has a round screen
     * @return true if the device has a round screen
     * @since 3.3.0
     */
    boolean isScreenRound();

    /**
     * Indicates if the device has a notch that modifies the display or not.
     *
     * @return true if the device has a notch, false 
     * otherwise
     * @since 3.8.0
     */
    boolean hasNotch();

    /**
     * Property that contains the position of the notch, if any, and can be used 
     * to track changes in its location.
     *
     * @return A read only property with the position of the notch if present
     * @since 3.8.0
     */
    ReadOnlyObjectProperty<Notch> notchProperty();
}

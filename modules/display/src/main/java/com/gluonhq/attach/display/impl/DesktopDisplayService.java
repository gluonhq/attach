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
package com.gluonhq.attach.display.impl;

import com.gluonhq.attach.display.DisplayService;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.Logger;

public class DesktopDisplayService implements DisplayService {

    private static final Logger LOG = Logger.getLogger(DesktopDisplayService.class.getName());

    private final Dimension2D dimensions;

    public DesktopDisplayService() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        dimensions = new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public boolean isPhone() {
        return false;
    }

    @Override
    public boolean isTablet() {
        return "tablet".equals(System.getProperty("charm-desktop-form"));
    }

    @Override
    public boolean isDesktop() {
        return true;
    }

    /**
     * Retrieve the dimension of the primary screen based on its bounds
     * @return Dimension of the Screen
     */
    @Override
    public Dimension2D getScreenResolution() {
        return dimensions;
    }

    @Override
    public Dimension2D getDefaultDimensions() {
        return isTablet() ? new Dimension2D(900, 600) : new Dimension2D(335, 600);
    }

    /**
     * Returns the screen scale of the primary screen
     * @return the screen scale
     */
    @Override
    public float getScreenScale() {
        return (float) Math.min(Screen.getPrimary().getOutputScaleX(), Screen.getPrimary().getOutputScaleY());
    }

    @Override
    public boolean isScreenRound() {
        return false;
    }

    @Override
    public boolean hasNotch() {
        return false;
    }

    @Override
    public ReadOnlyObjectProperty<Notch> notchProperty() {
        return new ReadOnlyObjectWrapper<>(Notch.UNKNOWN).getReadOnlyProperty();
    }

    private static void log(String message, Throwable cause) {
        LOG.log(Level.FINE, message);
        if (LOG.isLoggable(Level.FINE)) {
            cause.printStackTrace();
        }
    }
}
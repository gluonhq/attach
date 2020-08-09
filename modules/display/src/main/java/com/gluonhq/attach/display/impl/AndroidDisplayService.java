/*
 * Copyright (c) 2016, 2020 Gluon
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
import com.gluonhq.attach.util.Util;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidDisplayService implements DisplayService {

    private static final Logger LOG = Logger.getLogger(AndroidDisplayService.class.getName());

    private static final boolean debug = Util.DEBUG;

    static {
        System.loadLibrary("display");
    }

    public AndroidDisplayService() {
    }

    @Override
    public boolean isPhone() {
        return isPhoneFactor();
    }

    @Override
    public boolean isTablet() {
        return !isPhone();
    }

    @Override
    public boolean isDesktop() {
        return false;
    }

    /**
     * Retrieve the dimension of the primary screen based on its bounds
     * @return Dimension of the Screen
     */
    @Override
    public Dimension2D getScreenResolution() {
        double[] dim = screenSize();
        Dimension2D dimension2D = new Dimension2D(dim[0], dim[1]);
        if (debug) {
            LOG.log(Level.INFO, "Screen resolution: " + dimension2D);
        }
        return dimension2D;
    }

    @Override
    public Dimension2D getDefaultDimensions() {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Dimension2D dimension2D = new Dimension2D(visualBounds.getWidth(), visualBounds.getHeight());
        if (debug) {
            LOG.log(Level.INFO, "Screen default dimensions: " + dimension2D);
        }
        return dimension2D;
    }

    /**
     * Returns the screen scale of the primary screen
     * @return the screen scale
     */
    @Override
    public float getScreenScale() {
        float scale = (float) Math.min(Screen.getPrimary().getOutputScaleX(), Screen.getPrimary().getOutputScaleY());
        if (debug) {
            LOG.log(Level.INFO, "Screen scale: " + scale);
        }
        return scale;
    }

    @Override
    public boolean isScreenRound() {
        return screenRound();
    }

    @Override
    public boolean hasNotch() {
        return false;
    }

    @Override
    public ReadOnlyObjectProperty<Notch> notchProperty() {
        return new ReadOnlyObjectWrapper<>(Notch.UNKNOWN).getReadOnlyProperty();
    }

    // native
    private native static boolean isPhoneFactor();
    private native static double[] screenSize();
    private native static boolean screenRound();
}
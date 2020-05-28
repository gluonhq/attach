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
package com.gluonhq.attach.util;

import com.gluonhq.attach.util.impl.Debug;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * An enumeration of the platforms that are supported by Attach.
 *
 * @since 3.0.0
 */
public enum Platform {

    /**
     * The desktop platform provides implementations to access the applicable services available on
     * desktop operating systems (such as Windows, Linux, and Mac OS).
     */
    DESKTOP("Desktop"),

    /**
     * The android platform provides implementations to access the applicable services available on Android.
     */
    ANDROID("Android"),

    /**
     * The ios platform provides implementations to access the applicable services available on iOS.
     */
    IOS("IOS");


    private final String javafxPlatformName;

    Platform(String javafxPlatformName) {
        this.javafxPlatformName = javafxPlatformName;
    }

    private static Platform current;

    private static final Logger LOGGER = Logger.getLogger(Platform.class.getName());

    static {
        String platform = System.getProperty("javafx.platform", null);
        if (platform == null) {
            LOGGER.info("javafx.platform is not defined. Desktop will be assumed by default.");
            platform = DESKTOP.getName();
        }

        String name = platform.toUpperCase(Locale.ROOT);
        current = valueOf(name);
        LOGGER.fine("Current platform: "  + current);

        if (isAndroid() || isIOS()) {
            System.loadLibrary(isAndroid() ? "util" : "Util");
            Debug.init();
        }
    }

    /**
     * Returns the current platform that the code is being executed on. This obviously won't change during the
     * execution of the code.
     * @return The current {@link Platform}.
     */
    public static Platform getCurrent() {
        return current;
    }

    /**
     * Returns whether the current platform is desktop.
     * @return True if the current platform is desktop.
     */
    public static boolean isDesktop() {
        return DESKTOP == getCurrent();
    }

    /**
     * Returns whether the current platform is android.
     * @return True if the current platform is android.
     */
    public static boolean isAndroid() {
        return ANDROID == getCurrent();
    }

    /**
     * Returns whether the current platform is iOS.
     * @return True if the current platform is iOS.
     */
    public static boolean isIOS() {
        return IOS == getCurrent();
    }

    public final String getName() {
        return javafxPlatformName;
    }

}

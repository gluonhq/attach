/*
 * Copyright (c) 2016, 2026, Gluon
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
package com.gluonhq.attach.browser.impl;


import com.gluonhq.attach.browser.BrowserService;
import javafx.application.Platform;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class IOSBrowserService implements BrowserService {

    private static final Logger LOG = Logger.getLogger(IOSBrowserService.class.getName());

    private static Consumer<String> authCallback;

    static {
        System.loadLibrary("Browser");
        initBrowser();
    }

    @Override
    public void launchExternalBrowser(String url) throws IOException {
        if (!launchURL(url)) {
            throw new IOException("Error launching url " + url);
        }
    }

    @Override
    public void launchWebAuthentication(String url, String callbackUrlScheme, Consumer<String> callback) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IOException("Authentication url cannot be null or empty");
        }
        if (callbackUrlScheme == null || callbackUrlScheme.isEmpty()) {
            throw new IOException("Callback url scheme cannot be null or empty");
        }
        authCallback = callback;
        startWebAuthentication(url, callbackUrlScheme);
    }

    // native
    private native boolean launchURL(String url);
    private static native void initBrowser();
    private static native void startWebAuthentication(String url, String callbackUrlScheme);

    // callback
    public static void setAuthResult(String callbackUrl) {
        final Consumer<String> callback = authCallback;
        authCallback = null;
        if (callback == null) {
            LOG.warning("No callback registered for web authentication result");
            return;
        }
        Platform.runLater(() -> callback.accept(callbackUrl));
    }

}

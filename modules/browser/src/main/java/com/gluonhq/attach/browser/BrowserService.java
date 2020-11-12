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
package com.gluonhq.attach.browser;

import com.gluonhq.attach.util.Services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Launches the default browser of the platform as a separate application process. The browser
 * will be opened with the provided URL.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code BrowserService.create().ifPresent(service -> {
 *      service.launchExternalBrowser("https://gluonhq.com/");
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface BrowserService {

    /**
     * Returns an instance of {@link BrowserService}.
     * @return An instance of {@link BrowserService}.
     */
    static Optional<BrowserService> create() {
        return Services.get(BrowserService.class);
    }

    /**
     * Launches the user-default browser to show a specified URL.
     *
     * @param url The URL to load when the browser application opens.
     * @throws java.io.IOException If the URL can't be opened
     * @throws java.net.URISyntaxException If it is not a valid URL string
     */
    void launchExternalBrowser(String url) throws IOException, URISyntaxException;
}

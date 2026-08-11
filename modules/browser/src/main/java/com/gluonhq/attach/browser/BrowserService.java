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
package com.gluonhq.attach.browser;

import com.gluonhq.attach.util.Services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Launches the default browser of the platform as a separate application process. The browser
 * will be opened with the provided URL by means of {@link #launchExternalBrowser(String)}.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code BrowserService.create().ifPresent(service -> {
 *      service.launchExternalBrowser("https://gluonhq.com/");
 *  });}</pre>
 *
 * <p>The service can also be used to perform secure user authentication against a web service
 * (for instance an OAuth 2.0 / OpenID Connect provider), using an embedded browser, by means of
 * {@link #launchWebAuthentication(String, String, Consumer)}.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code BrowserService.create().ifPresent(service -> {
 *      service.launchWebAuthentication(
 *              "https://my-auth-provider.com/authorize?response_type=token&redirect_uri=myapp://callback",
 *              "myapp",
 *              callbackUrl -> {
 *                  if (callbackUrl != null) {
 *                      System.out.println("Authenticated, callback: " + callbackUrl);
 *                  } else {
 *                      System.out.println("Authentication cancelled or failed");
 *                  }
 *              });
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

    /**
     * Starts a web authentication session that lets the user authenticate against a web
     * service, and delivers the redirect (callback) URL back to the app once the
     * authentication flow completes.
     *
     * <p>On <b>iOS</b> this is implemented with a secure, dedicated native web view on top of the app.
     * When the web service redirects to a URL that matches {@code callbackUrlScheme},
     * the session is automatically dismissed and the full callback URL with the authorization code is
     * passed to {@code callback}. The redirect is secure and never travels through the system URL dispatch.</p>
     *
     * <p>The {@code callbackUrlScheme} can be provided in two forms:</p>
     * <ul>
     *   <li><b>A custom URL scheme</b> (without {@code ://}, e.g. {@code "myapp"}), with a
     *   redirect like {@code myapp://callback}. No {@code Info.plist} URL scheme registration is
     *   required, since the session intercepts the redirect on its own.
     *   </li>
     *   <li><b>A full HTTPS URL</b> (e.g. {@code "https://example.com/callback"}), with a
     *   verified HTTPS redirect. This requires iOS 17.4 or higher and the following setup:
     *     <ul>
     *       <li>The <b>Associated Domains</b> capability enabled on an explicit App ID (a wildcard
     *       App ID cannot carry it), and a provisioning profile that grants it.</li>
     *       <li>The app's entitlements must declare the domain under the {@code webcredentials}
     *       service type (e.g. {@code webcredentials:example.com}).</li>
     *       <li>An {@code apple-app-site-association} file hosted at
     *       {@code https://example.com/.well-known/apple-app-site-association} (served as
     *       {@code application/json}, no redirects) containing a {@code webcredentials} section that
     *       lists the app, e.g. {@code {"webcredentials":{"apps":["TEAMID.bundle.id"]}}}.</li>
     *       <li>For local development with a development-signed build (where the file is not on
     *       Apple's CDN), append {@code ?mode=developer} to the entitlement domain and enable
     *       <i>Settings &gt; Developer &gt; Associated Domains Development</i> on the device.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>iOS Configuration</b>: none for the custom-scheme form; the HTTPS form requires the
     * Associated Domains capability and {@code webcredentials} entitlement described above.</p>
     *
     * <p>On <b>Android</b> this is implemented with an
     * <a href="https://developer.chrome.com/docs/android/custom-tabs/guide-auth-tab">Auth Tab</a>,
     * a specialized Custom Tab designed for authentication flows, launched on top of the app.
     * When the web service redirects to a URL that matches {@code callbackUrlScheme}, the tab
     * is automatically dismissed and the full callback URL is passed to {@code callback},
     * without going through the system URL dispatch. Both the custom-scheme form (no manifest
     * setup required) and the HTTPS form (which requires
     * <a href="https://developers.google.com/digital-asset-links">Digital Asset Links</a>
     * verification: an {@code assetlinks.json} file hosted at
     * {@code https://example.com/.well-known/assetlinks.json} listing the app's package name and
     * signing certificate fingerprint) are supported. Auth Tab requires the default browser to
     * support it (e.g. Chrome 137+). On devices where it is not supported, the same intent
     * automatically falls back to a regular
     * <a href="https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs">Custom
     * Tab</a>: in that case the redirect is not captured by the tab but dispatched by the system,
     * so it requires an intent filter matching the callback URL scheme, declared for the activity
     * {@code com.gluonhq.helloandroid.WebAuthCallbackActivity} in the {@code AndroidManifest.xml}:
     *
     * <pre>{@code
     * <activity android:name="com.gluonhq.helloandroid.WebAuthCallbackActivity"
     *         android:exported="true"
     *         android:configChanges="keyboardHidden|orientation|screenSize">
     *     <intent-filter>
     *         <action android:name="android.intent.action.VIEW"/>
     *         <category android:name="android.intent.category.DEFAULT"/>
     *         <category android:name="android.intent.category.BROWSABLE"/>
     *         <data android:scheme="yourScheme"/>
     *     </intent-filter>
     * </activity>
     * }</pre>
     *
     * <p>With that in place, the redirect is delivered to {@code callback} exactly as in the
     * Auth Tab case, so the fallback is transparent to the application. Since Auth Tab support
     * depends on the user's default browser at runtime and cannot be known in advance, apps
     * distributed to arbitrary devices should always declare this intent filter: it is harmless
     * when Auth Tab is available, as the tab intercepts the redirect before it reaches the
     * system dispatch.</p>
     *
     * <p>On <b>Desktop</b> the default implementation simply opens the URL in the
     * external browser (see {@link #launchExternalBrowser(String)}), and the redirect has to be
     * handled by the application itself (for instance with a local HTTP server listening for a
     * {@code http://localhost} redirect).</p>
     *
     * @param url the authentication URL to load, including the {@code redirect_uri} expected by the
     *            web service.
     * @param callbackUrlScheme either a custom URL scheme (without {@code ://}, e.g. {@code "myapp"})
     *                          or a full HTTPS URL (e.g. {@code "https://example.com/callback"}) that
     *                          the web service uses for its redirect.
     * @param callback a consumer that receives the full callback URL on success, or {@code null} if
     *                 the user canceled the flow or an error occurred.
     * @throws java.io.IOException If the URL can't be opened
     * @throws java.net.URISyntaxException If it is not a valid URL string
     * @since 4.0.25
     */
    void launchWebAuthentication(String url, String callbackUrlScheme, Consumer<String> callback)
            throws IOException, URISyntaxException;
}

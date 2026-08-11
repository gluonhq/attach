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
     * without going through the system URL dispatch.</p>
     *
     * <p>Auth Tab requires the user's default browser to support it (e.g. Chrome 132+). Since that
     * cannot be known in advance, on devices where it is not supported the same intent
     * automatically falls back to a regular
     * <a href="https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs">Custom
     * Tab</a>: in that case the redirect is not captured by the tab but dispatched by the system
     * to the activity {@code com.gluonhq.helloandroid.WebAuthCallbackActivity}, provided by this
     * service, which delivers it to {@code callback} exactly as in the Auth Tab case. The system
     * dispatch requires an intent filter for that activity in the {@code AndroidManifest.xml},
     * with the requirements below for each of the two {@code callbackUrlScheme} forms. The
     * intent filter is harmless when Auth Tab is available (the tab intercepts the redirect
     * before it reaches the system dispatch), so apps distributed to arbitrary devices should
     * always declare it.</p>
     *
     * <ul>
     *   <li><b>The custom URL scheme form</b> (e.g. {@code "myapp"}), with a redirect like
     *   {@code myapp://callback}:
     *     <ul>
     *       <li>Auth Tab: no setup required.</li>
     *       <li>Custom Tab fallback: an intent filter for the scheme:
     * <pre>{@code
     * <activity android:name="com.gluonhq.helloandroid.WebAuthCallbackActivity"
     *         android:exported="true"
     *         android:configChanges="keyboardHidden|orientation|screenSize">
     *     <intent-filter>
     *         <action android:name="android.intent.action.VIEW"/>
     *         <category android:name="android.intent.category.DEFAULT"/>
     *         <category android:name="android.intent.category.BROWSABLE"/>
     *         <data android:scheme="myapp"/>
     *     </intent-filter>
     * </activity>
     * }</pre>
     *       </li>
     *     </ul>
     *   </li>
     *   <li><b>The full HTTPS URL form</b> (e.g. {@code "https://example.com/callback"}), with a
     *   verified HTTPS redirect. In both cases this requires
     *   <a href="https://developers.google.com/digital-asset-links">Digital Asset Links</a>
     *   verification: an {@code assetlinks.json} file hosted at
     *   {@code https://example.com/.well-known/assetlinks.json} (served as
     *   {@code application/json}, no redirects), listing the app's package name and signing
     *   certificate SHA-256 fingerprint with the
     *   {@code delegate_permission/common.handle_all_urls} relation.
     *     <ul>
     *       <li>Auth Tab: no manifest setup required; the browser verifies the calling app
     *       against the hosted {@code assetlinks.json} on its own.</li>
     *       <li>Custom Tab fallback: the redirect must additionally be a verified
     *       <a href="https://developer.android.com/training/app-links">App Link</a>, which
     *       requires an {@code android:autoVerify} intent filter for the domain (verification
     *       runs at install time against the same {@code assetlinks.json}):
     * <pre>{@code
     * <activity android:name="com.gluonhq.helloandroid.WebAuthCallbackActivity"
     *         android:exported="true"
     *         android:configChanges="keyboardHidden|orientation|screenSize">
     *     <intent-filter android:autoVerify="true">
     *         <action android:name="android.intent.action.VIEW"/>
     *         <category android:name="android.intent.category.DEFAULT"/>
     *         <category android:name="android.intent.category.BROWSABLE"/>
     *         <data android:scheme="https" android:host="example.com" android:path="/callback"/>
     *     </intent-filter>
     * </activity>
     * }</pre>
     *       The verification state can be checked with
     *       {@code adb shell pm get-app-links <package.name>}. Note that with the fallback the
     *       redirect travels through the browser as a regular navigation, so the web service
     *       should use the authorization-code flow ({@code response_type=code}): a redirect
     *       carrying parameters in the URL fragment (e.g. the implicit flow
     *       {@code #access_token=...}) is not reliably preserved across the system dispatch.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Android Configuration</b>: none for Auth Tab browsers with the custom-scheme form;
     * the intent filters described above for the Custom Tab fallback, and the hosted
     * {@code assetlinks.json} for the HTTPS form.</p>
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

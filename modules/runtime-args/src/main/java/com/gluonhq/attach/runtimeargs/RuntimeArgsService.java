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
package com.gluonhq.attach.runtimeargs;

import com.gluonhq.attach.util.Services;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * The runtime args service allows for external events to be fired into Gluon Attach
 * applications - these events are either provided at startup time as runtime arguments
 * to the application, or they may be delivered at runtime by external services. Developers
 * may choose to observe these events by {@link #addListener(String, Consumer) adding a listener}.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code RuntimeArgsService.create().ifPresent(service -> {
 *      service.addListener("ALERT", value -> {
 *              // show alert(value)
 *      });
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>:</p>
 *
 * To launch the app from a custom URL, like yourScheme://foo.html, register a
 * custom scheme in the AndroidManifest.xml file:
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 *
 * {@code
    <activity android:name="com.gluonhq.helloandroid.LaunchURLActivity"
            android:launchMode="singleTask"
            android:configChanges="keyboardHidden|orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.VIEW"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <category android:name="android.intent.category.BROWSABLE"/>
                <data android:scheme="yourScheme"/>
            </intent-filter>
    </activity>
 * }
 *
 * To launch the app from a Local Notification, add the following to the
 * AndroidManifest.xml file:
 *
 *<pre>
 * {@code
 * <activity android:name="javafxports.android.FXActivity"
 *           android:launchMode="singleTop" ...>
 * </activity>

 *  <activity android:name="com.gluonhq.attach.runtime.android.impl.NotificationActivity"
 *            android:parentActivityName="javafxports.android.FXActivity">
 *       <meta-data android:name="android.support.PARENT_ACTIVITY"
 *                  android:value="javafxports.android.FXActivity"/>
 *   </activity>
 *   <receiver android:name="com.gluonhq.attach.runtime.android.impl.AlarmReceiver" />
 * }</pre>
 *
 * <p><b>iOS Configuration</b>:
 *
 * To launch the app from a custom URL, like yourScheme://foo.html, register a
 * custom scheme in the plist file:
 *
 * {@code
    <key>CFBundleURLTypes</key>
    <array>
        <dict>
            <key>CFBundleURLName</key>
            <string>your.java.package.YourScheme</string>
            <key>CFBundleURLSchemes</key>
            <array>
                <string>yourScheme</string>
            </array>
        </dict>
    </array>
 * }
 *
 * To launch the app from another app, the latter has to
 * include in its plist file:
 *
 * {@code
    <key>LSApplicationQueriesSchemes</key>
    <array>
        <string>yourScheme</string>
    </array>
 * }
 * </p>
 *
 * @since 3.1.0
 */
public interface RuntimeArgsService {

    /**
     * Key used in case the app is launched from an URL with a custom scheme
     */
    String LAUNCH_URL_KEY = "Launch.URL";

    /**
     * Key used in case the app is launched from a local notification
     */
    String LAUNCH_LOCAL_NOTIFICATION_KEY = "Launch.LocalNotification";

    /**
     * Key used in case the app is launched from a push notification
     */
    String LAUNCH_PUSH_NOTIFICATION_KEY = "Launch.PushNotification";

    /**
     * Returns an instance of {@link RuntimeArgsService}.
     * @return An instance of {@link RuntimeArgsService}.
     */
    static Optional<RuntimeArgsService> create() {
        return Services.get(RuntimeArgsService.class);
    }

    /**
     * When the app is launched externally, this method is called to identify the key based on
     * the service that is responsible, and the value associated to this key.
     *
     * Typically the developer won't need to call this method for certain services that
     * will launch the app from a Local Notifications or a custom URL.
     *
     * @param key a String that the service is expected to receive
     * @param value a String that the service will process when the key is received
     */
    void fire(String key, String value);

    /**
     * Adds a listener to the given key, like {@link #LAUNCH_LOCAL_NOTIFICATION_KEY} or
     * {@link #LAUNCH_URL_KEY}, so if those occur, the consumer will accept the provided
     * String
     *
     * @param key a String that the service is expected to receive
     * @param consumer the operation that will be accepted
     */
    void addListener(String key, Consumer<String> consumer);

    /**
     * Removes the listener for the given key
     * @param key a String that the service is expected to receive
     */
    void removeListener(String key);
}

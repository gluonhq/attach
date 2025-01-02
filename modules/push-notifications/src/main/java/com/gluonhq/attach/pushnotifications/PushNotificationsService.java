/*
 * Copyright (c) 2016, 2025, Gluon
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
package com.gluonhq.attach.pushnotifications;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Optional;

/**
 * Adds the ability for the application to receive remote push notifications.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code PushNotificationsService.create().ifPresent(service -> {
 *       service.tokenProperty.addListener((observable, oldValue, newValue) -> {
 *           if (newValue != null) {
 *               String deviceToken = newValue;
 *               // This deviceToken can be used to send push notifications to this device by calling
 *               // the appropriate server API for the platform. Usually, this token is sent to a server
 *               // where the push notifications are created and sent out.
 *           }
 *       });
 *       service.register();
 *   });}</pre>
 *
 * <p><b>Requirements</b></p>
 *
 * <p>The service requires the following changes on Android and iOS.</p>
 *
 * <p>However, these are handled automatically by the <a href="https://docs.gluonhq.com/">GluonFX plugin</a>,
 * when used.</p>
 *
 * <p><b>Android Configuration</b></p>
 * <p>To enable push notifications on android, an existing <a href="https://console.firebase.google.com">Google Firebase project</a>
 * is required. Copy the <code>google-services.json</code> file into your project's <code>src/android/resources</code> folder.</p>
 * <p>The following <code>permissions</code>, <code>services</code> and <code>receiver</code> need to be added to the
 * android manifest configuration file to make push notifications work on android. The main activity also requires the
 * attribute <code>android:launchMode</code> with value <code>singleTop</code>.</p>
 * <pre>
 * {@code <manifest ...>
 *    ...
 *    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
 *    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *    ...
 *    <application ...>
 *      <activity android:name="com.gluonhq.helloandroid.MainActivity"
 *                android:configChanges="orientation|keyboardHidden"
 *                android:launchMode="singleTop">
 *        ...
 *      </activity>
 *      ...
 *      <service android:name="com.gluonhq.helloandroid.PushFcmMessagingService">
 *        <intent-filter>
 *          <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *        </intent-filter>
 *      </service>
 *      <service android:name="com.gluonhq.helloandroid.PushNotificationJobService"
 *               android:permission="android.permission.BIND_JOB_SERVICE"
 *               android:exported="true" />
 *      <activity android:name="com.gluonhq.helloandroid.PushNotificationActivity"
 *                android:parentActivityName="com.gluonhq.helloandroid.MainActivity">
 *            <meta-data android:name="android.support.PARENT_ACTIVITY"
 *                       android:value="com.gluonhq.helloandroid.MainActivity"/>
 *      </activity>
 *      <receiver android:name="com.gluonhq.helloandroid.FirebaseBroadcastReceiver"
 *                android:exported="true"
 *                android:permission="com.google.android.c2dm.permission.SEND">
 *             <intent-filter>
 *                 <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *             </intent-filter>
 *      </receiver>
 *    </application>
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 *
 * <p>You need to use a Provisioning Profile that defines the <code>aps-environment</code> property in the
 * <code>Entitlements</code> section to either <code>development</code> or <code>production</code>.</p>
 *
 * @since 3.2.0
 */
public interface PushNotificationsService {

    /**
     * Returns an instance of {@link PushNotificationsService}.
     * @return An instance of {@link PushNotificationsService}.
     */
    static Optional<PushNotificationsService> create() {
        return Services.get(PushNotificationsService.class);
    }

    /**
     * The unique registration token that can be passed to the server, so the app
     * can receive push notifications
     *
     * @return a ReadOnlyStringProperty with a unique token
     */
    ReadOnlyStringProperty tokenProperty();

    /**
     * Register the app for receiving push notifications. On iOS this will trigger a confirmation dialog that
     * must be accepted by the user.
     * @since  4.0.10
     */
    void register();

    /**
     * Register the app for receiving push notifications. On iOS this will trigger a confirmation dialog that
     * must be accepted by the user. For Android, you need to pass in the authorizedEntity value that matches the
     * Sender ID of your Google Cloud Messaging or Firebase Cloud Messaging application.
     *
     * @param authorizedEntity a string that matches the Sender ID of a GCM or FCM application
     * @deprecated
     */
    @Deprecated
    void register(String authorizedEntity);

    /**
     * On iOS, set the number showing as the badge of the app icon on the Home screen.
     * Set number to 0 to hide the badge number.
     * On Android, override the default number of notifications displayed in the popup
     * that can be shown from the app icon. Set 0 to use the default notification
     * counter.
     *
     * Important notes:
     * - there is no track of the current badge number that the icon might have
     * - when the push notification is read, there is no automatic handling of
     *   decreasing the badge number
     * So it is up to the developer to handle this number properly.
     *
     * @param badgeNumber the number that will show up in the app icon badge,
     *                    or 0 to hide it. The default value is 0.
     */
    void setAppIconBadgeNumber(int badgeNumber);

    /**
     * Removes all delivered notifications that haven't been read yet.
     * This can be used to remove pending notifications, when the user doesn't tap on
     * the notification to open the app, but directly opens it.
     * It also resets the badge number.
     * @since 4.0.22
     */
    void removeAllNotifications();
}

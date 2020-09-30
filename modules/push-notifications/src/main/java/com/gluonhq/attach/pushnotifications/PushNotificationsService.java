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
package com.gluonhq.attach.pushnotifications;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Optional;

/**
 * Adds the ability for the application to receive remote push notifications.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code String senderId = "abcd1234";
 *  PushNotificationsService.create().ifPresent(service -> {
 *      service.register(senderId);
 *      service.tokenProperty.addListener((observable, oldValue, newValue) -> {
 *          if (newValue != null) {
 *              String deviceToken = newValue;
 *              // This deviceToken can be used to send push notifications to this device by calling
 *              // the appropriate server API for the platform. Usually, this token is sent to a server
 *              // where the push notifications are created and sent out.
 *          }
 *      });
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The following <code>permissions</code>, <code>services</code> and <code>receiver</code> need to be added to the
 * android manifest configuration file to make push notifications work on android. The main activity also requires the
 * attribute <code>android:launchMode</code> with value <code>singleTop</code>.</p>
 * <p>Also, make sure that you replace <code>$packageName</code> with the value of the <code>package</code> attribute
 * in the <code>manifest</code> element that is defined at the top of your AndroidManifest.xml. You should have replaced
 * the <code>$packageName</code> string three times.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    ...
 *    <permission android:name="$packageName.permission.C2D_MESSAGE" android:protectionLevel="signature" />
 *    <uses-permission android:name="$packageName.permission.C2D_MESSAGE" />
 *    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
 *    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *    <uses-permission android:name="android.permission.WAKE_LOCK" />
 *    ...
 *    <application ...>
 *      <activity android:name="javafxports.android.FXActivity"
 *                android:label="SampleGluonApp"
 *                android:launchMode="singleTop"
 *                android:configChanges="orientation|screenSize">
 *        <meta-data android:name="main.class" android:value="com.gluonhq.sample.SampleGluonApp"/>
 *        ...
 *      </activity>
 *      ...
 *      <receiver android:name="com.google.android.gms.gcm.GcmReceiver"
 *                android:exported="true"
 *                android:permission="com.google.android.c2dm.permission.SEND" >
 *        <intent-filter>
 *          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *          <category android:name="$packageName" />
 *        </intent-filter>
 *      </receiver>
 *      <service android:name="com.gluonhq.impl.attach.plugins.android.PushNotificationJobService"
 *               android:permission="android.permission.BIND_JOB_SERVICE"
 *               android:exported="true" />
 *      <service android:name="com.gluonhq.impl.attach.plugins.pushnotifications.android.PushGcmListenerService"
 *               android:exported="false">
 *        <intent-filter>
 *          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *        </intent-filter>
 *      </service>
 *      <service android:name="com.gluonhq.impl.attach.plugins.pushnotifications.android.PushInstanceIDListenerService"
 *               android:exported="false">
 *        <intent-filter>
 *          <action android:name="com.google.android.gms.iid.InstanceID" />
 *        </intent-filter>
 *      </service>
 *      <service android:name="com.gluonhq.impl.attach.plugins.pushnotifications.android.RegistrationIntentService"
 *               android:exported="false">
 *      </service>
 *      <activity android:name="com.gluonhq.impl.attach.plugins.pushnotifications.android.PushNotificationActivity"
 *                android:parentActivityName="javafxports.android.FXActivity">
 *            <meta-data android:name="android.support.PARENT_ACTIVITY"
 *                       android:value="javafxports.android.FXActivity"/>
 *      </activity>
 *
 *      <meta-data android:name="com.google.android.gms.version"
 *               android:value="9452000"/>
 *    </application>
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 *
 * <p>You need to make sure that you set the <code>jfxmobile.ios.apsEnvironment</code> property in the
 * <code>build.gradle</code> of your project to either <code>development</code> or <code>production</code>, matching the
 * configured provisioning profile.</p>
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
     * must be accepted by the user. For Android, you need to pass in the authorizedEntity value that matches the
     * Sender ID of your Google Cloud Messaging or Firebase Cloud Messaging application.
     *
     * @param authorizedEntity a string that matches the Sender ID of a GCM or FCM application
     */
    void register(String authorizedEntity);
}

/*
 * Copyright (c) 2016, 2020, Gluon
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
package com.gluonhq.attach.localnotifications;

import com.gluonhq.attach.util.Services;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * Used to schedule a native notification.
 * 
 * <p>In the simple scenario where the same application instance is running when the native notification fires (and
 * is subsequently clicked on), the app will resume (if it was in the background), and the notification
 * {@link Notification#getRunnable() Runnable} will be executed.</p>
 *
 * <p>By and large notifications 'just work', but there is one scenario to be aware of. This is
 * the situation where a native notification fires when the application is either completely closed
 * (i.e. not even running 'in the background'), or when the application that created the notifications
 * is not the same instance as the one receiving the notifications.</p>
 *
 * <p>In these cases, it is important to remember that the application has the responsibility of restoring
 * notifications at startup. Typically the developer will have to load upon startup all the notifications that
 * were created on the first place, call 
 * {@link #getNotifications() } to get access to the observable list of notifications and 
 * call {@link ObservableList#addAll(java.lang.Object...) addAll(Notification...) } or 
 * {@link ObservableList#addAll(java.util.Collection) }
 * every time the application is opened. Using {@link ObservableList#add(java.lang.Object) add(Notification)}
 * is not advisable in case of having more than one notification.</p> 
 *
 * <p>Doing this on every startup does not have the effect of 'duplicating' the
 * same notifications - provided the {@link Notification#getId() ID} of the notification remains constant 
 * between runs, the native platform will deliver it only once.</p>
 * 
 * <p>But the developer has the possibility to remove the notification, once it has been delivered, by avoiding
 * registering it all over again once the scheduled time is in the past. Note that any notification 
 * scheduled for a past time will be fired immediately.
 * Note as well that if either the notification's scheduled date or its text are null or empty, 
 * the notification won't be scheduled on the device.</p>
 * 
 * <p><b>Example</b></p>
 * <pre>
 * {@code String notificationId = "abcd1234";
 *  LocalNotificationsService.create().ifPresent(service -> {
 *      service.getNotifications().add(new Notification(notificationId, "Sample Notification Text",
 *              ZonedDateTime.now().plusSeconds(20), () -> {
 *                      Alert alert = new Alert(AlertType.INFORMATION, "You have been notified!");
 *                      Platform.runLater(() -> alert.showAndWait());
 *              }));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The following <code>activity</code> and <code>receiver</code> need to be added to the android manifest
 * configuration file to make local notifications work on android. The main activity also requires the attribute
 * <code>android:launchMode</code> with value <code>singleTop</code>.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    ...
 *    <activity android:name='com.gluonhq.helloandroid.MainActivity'
 *                   android:configChanges="orientation|keyboardHidden"
 *                   android:launchMode="singleTop">
 *             <intent-filter>
 *                 <category android:name='android.intent.category.LAUNCHER'/>
 *                 <action android:name='android.intent.action.MAIN'/>
 *             </intent-filter>
 *         </activity>
 *         <activity android:name="com.gluonhq.helloandroid.NotificationActivity"
 *             android:parentActivityName="com.gluonhq.helloandroid.MainActivity">
 *             <meta-data android:name="android.support.PARENT_ACTIVITY"
 *                 android:value="com.gluonhq.helloandroid.MainActivity"/>
 *         </activity>
 *         <receiver android:name="com.gluonhq.helloandroid.AlarmReceiver" />
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @see Notification
 * @since 3.0.0
 */
public interface LocalNotificationsService {

    /**
     * Returns an instance of {@link LocalNotificationsService}.
     * @return An instance of {@link LocalNotificationsService}.
     */
    static Optional<LocalNotificationsService> create() {
        return Services.get(LocalNotificationsService.class);
    }

    /**
     * An Observable List of Notifications, that can be used to
     * add or remove notifications
     *
     * @return ObservableList of Notification
     */
    ObservableList<Notification> getNotifications();
}

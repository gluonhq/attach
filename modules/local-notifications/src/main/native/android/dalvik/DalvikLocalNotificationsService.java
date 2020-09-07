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
package com.gluonhq.helloandroid;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

public class DalvikLocalNotificationsService {

    private static final String TAG = Util.TAG;
    private static final String CHANNEL_ID = "gluon_attach_channel";

    private static Activity activity;

    public DalvikLocalNotificationsService(Activity activity) {
        DalvikLocalNotificationsService.this.activity = activity;
    }

    static Activity getActivity() {
        return activity;
    }

    private void scheduleNotification(String title, String text, String id, String imagePath, long millis) {
        PendingIntent pendingIntent = getPendingIntent(title, text, id, imagePath);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
        }
    }

    private void unscheduleNotification(String id) {
        PendingIntent pendingIntent = getPendingIntent(null, null, id, null);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    private PendingIntent getPendingIntent(String title, String text, String id, String imagePath) {
        // only process valid notifications
        if (id == null) {
            return null;
        }

        Intent notificationIntent = new Intent(activity, AlarmReceiver.class);
        // use the data field to define a unique intent, even if request code is the same
        notificationIntent.setData(getData(id));
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, id);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION, getNotification(title, text, id, imagePath));
        return PendingIntent.getBroadcast(activity, AlarmReceiver.REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification getNotification(String title, String text, String id, String imagePath) {

        Intent resultIntent = new Intent(activity, NotificationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setData(getData(id));
        resultIntent.putExtra(NotificationActivity.ID, id);
        resultIntent.putExtra(NotificationActivity.PACKAGE_NAME, activity.getPackageName());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(activity, AlarmReceiver.REQUEST_CODE,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(activity);
        if (title != null) {
            builder.setContentTitle(title);
        }
        builder.setContentText(text);
        builder.setSmallIcon(activity.getApplicationInfo().icon);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setContentIntent(resultPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_EVENT);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        if (imagePath != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(BitmapFactory.decodeFile(imagePath));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelId();
            builder.setChannelId(CHANNEL_ID);
        }
        builder.setAutoCancel(true);
        return builder.build();
    }

    // Provides unique Uri based on the id of the notification
    private Uri getData(String id) {
        return Uri.withAppendedPath(Uri.parse("gluon://attach/Id/#"), id);
    }

    private void createChannelId() {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel
        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        // The user-visible name of the channel.
        CharSequence name = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : activity.getString(stringId);
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

        // Configure the notification channel.
        mChannel.setShowBadge(true);
        notificationManager.createNotificationChannel(mChannel);
    }
}

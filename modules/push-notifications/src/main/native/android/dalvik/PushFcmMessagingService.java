/*
 * Copyright (c) 2018, 2024, Gluon
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
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map.Entry;

public class PushFcmMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "gluon_attach_channel";
    private static final String TAG = Util.TAG;
    private static final boolean debug = Util.isDebug();

    /**
     * This is only called when app is in foreground. It needs data payload
     * and not only notification payload.
     * From Firebase Console: if only Notification Title/Notification Text are
     * set, it won't call onMessageReceived(). It needs also Custom data fields set
     * with title/body/id/silent
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (debug) {
            Log.v(TAG, "Message received from " + remoteMessage.getFrom());
        }

        HashMap<String, String> payload = new HashMap<>(remoteMessage.getData());
        payload.putIfAbsent("id", "");
        payload.putIfAbsent("silent", "false");
        payload.putIfAbsent("title", "");
        payload.putIfAbsent("body", "");

        boolean silent = false;
        try {
            silent = Boolean.parseBoolean(payload.get("silent"));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing remote message data: " + remoteMessage.getData() + ", " + e.getMessage());
        }

        if (silent) {
            // invisible notification. Don't show, directly call into RAS
            if (debug) {
                Log.v(TAG, "Message will be processed through RAS");
            }

            String rasMessage = jsonPrintMap(payload);

            // set the message as system property in case app was closed, so later on, 
            // when resuming the app, RAS can handle it
            System.setProperty(DalvikPushNotificationsService.LAUNCH_PUSH_NOTIFICATION_KEY, rasMessage);
            // fire will work if the key is available, only when the app is running:
            DalvikPushNotificationsService.sendRuntimeArgs(rasMessage);
        } else {
            if (debug) {
                Log.v(TAG, "Message will be processed through a PushNotificationActivity");
            }
            sendNotification(payload);
        }
    }
  
    /**
     * Called if FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve
     * the token.
     */
    @Override
    public void onNewToken(String token) {
        if (debug) {
            Log.v(TAG, "New token: " + token);
        }
        sendToken(token);
    }

    static void retrieveToken() {
        // Retrieve token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                if (debug) {
                    Log.v(TAG, "Updated registration token: " + token);
                }
                sendToken(token);
            }
        });
    }

    private void sendNotification(HashMap<String, String> payload) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        if (debug) {
            Log.v(TAG, "Sending push notification with payload: " + jsonPrintMap(payload));
        }
        int requestCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        notificationManager.notify(requestCode, getNotification(requestCode, payload));
    }

    private String jsonPrintMap(HashMap<String, String> map) {
        String json = "";
        for (Entry<String, String> entry : map.entrySet()) {
            if (!json.isEmpty()) {
                json += ",";
            }
            json += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
        }
        return "{" + json + "}";
    }
	
    private Notification getNotification(int requestCode, HashMap<String, String> payload) {
        final Application application = getApplication();
        String id = payload.get("id");
        String title = payload.get("title");
        String body = payload.get("body");
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        Intent resultIntent = new Intent(application, PushNotificationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setData(getData(id));
        resultIntent.putExtra(PushNotificationActivity.MESSAGE, jsonPrintMap(payload));
        resultIntent.putExtra(PushNotificationActivity.PACKAGE_NAME, application.getPackageName());

        int flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent resultPendingIntent = PendingIntent.getActivity(application, requestCode,
                resultIntent, flag);

        android.app.Notification.Builder builder = new android.app.Notification.Builder(application);
        if (title != null) {
            builder.setContentTitle(title);
        }
        builder.setContentText(body);
        builder.setSmallIcon(application.getApplicationInfo().icon);
        builder.setPriority(android.app.Notification.PRIORITY_MAX);
        int number = DalvikPushNotificationsService.badgeNumber;
        if (number > 0) {
            if (debug) {
                Log.v(TAG, "Set badge number to: " + number);
            }
            builder.setNumber(number);
        }
        builder.setContentIntent(resultPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(android.app.Notification.CATEGORY_EVENT);
            builder.setVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        }
        builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //builder.setLargeIcon(BitmapFactory.decodeStream(notification.getImageInputStream()));
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
       return Uri.withAppendedPath(Uri.parse("charm://attach/Id/#"), id);
    }

    private void createChannelId() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Application application = getApplication();
        // The id of the channel
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        // The user-visible name of the channel.
        CharSequence name = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : application.getString(stringId);
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

        // Configure the notification channel.
        mChannel.setShowBadge(true);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private native static void sendToken(String token);
}

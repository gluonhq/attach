/*
 * Copyright (c) 2024, Gluon
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class FirebaseBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = Util.TAG;
    private static final boolean debug = Util.isDebug();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null || !"com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())) {
            return;
        }
        if (debug) {
            Log.v(TAG, "FirebaseBroadcastReceiver has intent");
        }
        String id = setValueOr(intent.getStringExtra("id"), intent.getStringExtra("google.message_id"));
        String title = setValueOr(intent.getStringExtra("title"), intent.getStringExtra("gcm.notification.title"));
        String body = setValueOr(intent.getStringExtra("body"), intent.getStringExtra("gcm.notification.body"));
        String silent = setValueOr(intent.getStringExtra("silent"), "false");

        HashMap<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("title", title);
        payload.put("body", body);
        payload.put("silent", silent);

        String rasMessage = jsonPrintMap(payload);


        if (DalvikPushNotificationsService.getActivity() != null) {
            if (!DalvikPushNotificationsService.isActive()) {
                // if we have an app running in the background
                if (debug) {
                    Log.v(TAG, "FirebaseBroadcastReceiver: App in background, processing notification through RAS");
                }
                System.setProperty(DalvikPushNotificationsService.LAUNCH_PUSH_NOTIFICATION_KEY, rasMessage);
                DalvikPushNotificationsService.sendRuntimeArgs(rasMessage);
            } else {
                // app is active, PushFcmMessagingService process and creates notification in PushNotificationActivity
            }
        } else {
            if (debug) {
                Log.v(TAG, "FirebaseBroadcastReceiver: App closed, Message will be processed through RAS");
            }
            // set the message as system property as app is closed, so later on,
            // when starting the app after tapping the notification, RAS can handle it
            System.setProperty(DalvikPushNotificationsService.LAUNCH_PUSH_NOTIFICATION_KEY, rasMessage);
        }
    }

    private String setValueOr(String first, String second) {
        return (first == null || first.isEmpty()) ? second : first;
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
}

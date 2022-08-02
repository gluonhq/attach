/*
 * Copyright (c) 2020, 2022, Gluon
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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import java.util.Arrays;

public class Util {

    public static final String TAG = "GluonAttach";

    private static Activity activity;
    private static IntentHandler intentHandler;
    private static LifecycleEventHandler lifecycleEventHandler;
    private static boolean debug = false;

    public Util(Activity activity) {
        this.activity = activity;
        Log.v(TAG, "Util <init>");
        syncClipboardFromOS();
    }

    private static void enableDebug() {
        debug = true;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setOnActivityResultHandler(IntentHandler handler) {
        Util.intentHandler = handler;
    }

    public static void setLifecycleEventHandler(LifecycleEventHandler lifecycleEventHandler) {
        Util.lifecycleEventHandler = lifecycleEventHandler;
    }

    public static boolean verifyPermissions(String... permissions) {
        if (debug) {
            Log.v(TAG, "Util::verifyPermissions for permissions: " + Arrays.toString(permissions));
        }
        Util util = new Util(null);
        return util.nativeVerifyPermissions(permissions);
    }

    private static void syncClipboardFromOS() {
        if (activity == null) {
            return;
        }
        Util.activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ClipboardManager clipboard = (ClipboardManager) Util.activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData data = clipboard.getPrimaryClip();
                            if (data == null) return;
                            ClipData.Item item = data.getItemAt(0);
                            if (item != null && item.getText() != null) {
                                if (debug) {
                                    Log.v(TAG, "Util::clipboardFromOS set text");
                                }
                                nativeSyncClipboardFromOS(item.getText().toString());
                            }
                        }
                    }
                }, 100);
            }
        });
    }

    private static void syncClipboardToOS() {
        if (activity == null) {
            return;
        }
        final String text = nativeSyncClipboardToOS();
        if (text != null && !text.isEmpty()) {
            Util.activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ClipboardManager clipboard = (ClipboardManager) Util.activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        if (debug) {
                            Log.v(TAG, "Util::clipboardToOS set text");
                        }
                        clipboard.setPrimaryClip(ClipData.newPlainText(text, text));
                    }
                }
            });
        }
    }

    private static void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (debug) {
            Log.v(TAG, "Util::onActivityResult with requestCode: " + requestCode + ", resultCode: " + resultCode + ", intent: " + intent);
        }
        if (Util.intentHandler != null) {
            Util.intentHandler.gotActivityResult(requestCode, resultCode, intent);
        }
    }

    private static void lifecycleEvent(String event) {
        if (Util.lifecycleEventHandler != null) {
            if (debug) {
                Log.v(TAG, "Util::lifecycleEvent with event: " + event);
            }
            Util.lifecycleEventHandler.lifecycleEvent(event);
        }

        if ("resume".equals(event)) {
            syncClipboardFromOS();
        } else if ("pause".equals(event)) {
            syncClipboardToOS();
        }
    }

    private native boolean nativeVerifyPermissions(String[] permissions);
    private static native void nativeSyncClipboardFromOS(String content);
    private static native String nativeSyncClipboardToOS();
}

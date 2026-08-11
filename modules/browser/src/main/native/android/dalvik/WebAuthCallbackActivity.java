/*
 * Copyright (c) 2026, Gluon
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Trampoline activity that receives the web authentication redirect when the browser does not
 * support Auth Tab and the intent falls back to a regular Custom Tab. In that case the redirect
 * is dispatched by the system instead of being captured by the tab, so this activity picks it up,
 * delivers the callback URL to the pending {@link DalvikBrowserService} session, and brings the
 * application back to the front (dismissing the Custom Tab).
 *
 * <p>It requires an intent filter matching the callback URL scheme in the AndroidManifest.xml:</p>
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
 */
public class WebAuthCallbackActivity extends Activity {

    private static final String TAG = Util.TAG;

    private final boolean debug;

    public WebAuthCallbackActivity() {
        debug = Util.isDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (debug) {
            Log.v(TAG, "WebAuthCallbackActivity :: received uri: " + uri);
        }
        if (uri != null && DalvikBrowserService.handleWebAuthCallback(uri)) {
            // bring the main activity back to front, dismissing the Custom Tab on top of it
            try {
                Class<?> clazz = Class.forName("com.gluonhq.helloandroid.MainActivity");
                Intent intent = new Intent(this, clazz);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "WebAuthCallbackActivity :: error " + e.getMessage());
            }
        } else if (debug) {
            Log.v(TAG, "WebAuthCallbackActivity :: no pending web authentication session for uri: " + uri);
        }
        finish();
    }
}

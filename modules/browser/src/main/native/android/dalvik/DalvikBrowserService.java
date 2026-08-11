/*
 * Copyright (c) 2020, 2026, Gluon
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

public class DalvikBrowserService {

    private static final String TAG = Util.TAG;

    private static final int WEB_AUTH_REQUEST_CODE = 20126;

    // Public intent extras from androidx.browser Auth Tab
    private static final String EXTRA_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_LAUNCH_AUTH_TAB = "androidx.browser.auth.extra.LAUNCH_AUTH_TAB";
    private static final String EXTRA_REDIRECT_SCHEME = "androidx.browser.auth.extra.REDIRECT_SCHEME";
    private static final String EXTRA_HTTPS_REDIRECT_HOST = "androidx.browser.auth.extra.HTTPS_REDIRECT_HOST";
    private static final String EXTRA_HTTPS_REDIRECT_PATH = "androidx.browser.auth.extra.HTTPS_REDIRECT_PATH";

    private final Activity activity;
    private final boolean debug;

    public DalvikBrowserService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
    }

    private boolean launchURL(String url) {
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "Invalid URL: url was null or empty");
            return false;
        }

        if (!(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("sms:") || url.startsWith("smsto:"))) {
            Log.e(TAG, "Invalid URL: url should start with http://, https://, sms:, or smsto:");
            return false;
        }

        if (debug) {
            Log.v(TAG, "Launching URL: " + url);
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (browserIntent.resolveActivity(activity.getPackageManager()) == null) {
            Log.e(TAG, "There is no activity to handle the browser intent");
            return false;
        }

        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(browserIntent);
        return true;
    }

    private void startWebAuthentication(String url, String callbackUrlScheme) {
        if (url == null || url.isEmpty() || callbackUrlScheme == null || callbackUrlScheme.isEmpty()) {
            Log.e(TAG, "Invalid web authentication parameters: url and callbackUrlScheme are required");
            nativeWebAuthResult(null);
            return;
        }

        Intent authIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        authIntent.putExtra(EXTRA_LAUNCH_AUTH_TAB, true);
        // null session so browsers without Auth Tab support treat this as a Custom Tab
        Bundle sessionBundle = new Bundle();
        sessionBundle.putBinder(EXTRA_SESSION, null);
        authIntent.putExtras(sessionBundle);

        if (callbackUrlScheme.startsWith("https://")) {
            Uri redirectUri = Uri.parse(callbackUrlScheme);
            String host = redirectUri.getHost();
            if (host == null || host.isEmpty()) {
                Log.e(TAG, "Invalid https callback url: " + callbackUrlScheme);
                nativeWebAuthResult(null);
                return;
            }
            authIntent.putExtra(EXTRA_HTTPS_REDIRECT_HOST, host);
            String path = redirectUri.getPath();
            authIntent.putExtra(EXTRA_HTTPS_REDIRECT_PATH, (path == null || path.isEmpty()) ? "/" : path);
        } else {
            authIntent.putExtra(EXTRA_REDIRECT_SCHEME, callbackUrlScheme);
        }

        if (authIntent.resolveActivity(activity.getPackageManager()) == null) {
            Log.e(TAG, "There is no activity to handle the web authentication intent");
            nativeWebAuthResult(null);
            return;
        }

        Util.setOnActivityResultHandler(new IntentHandler() {
            @Override
            public void gotActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode != WEB_AUTH_REQUEST_CODE) {
                    return;
                }
                Util.setOnActivityResultHandler(null);
                String callbackUrl = null;
                if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {
                    callbackUrl = intent.getData().toString();
                }
                if (debug) {
                    Log.v(TAG, "Web authentication result, code: " + resultCode + ", url: " + callbackUrl);
                }
                nativeWebAuthResult(callbackUrl);
            }
        });

        if (debug) {
            Log.v(TAG, "Launching web authentication with URL: " + url + ", callback scheme: " + callbackUrlScheme);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.startActivityForResult(authIntent, WEB_AUTH_REQUEST_CODE);
            }
        });
    }

    private native void nativeWebAuthResult(String callbackUrl);
}
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class LaunchURLActivity extends Activity {

    private static final String TAG = Util.TAG;
    private static final String LAUNCH_URL_KEY = "Launch.URL";

    private final boolean debug;

    public LaunchURLActivity() {
        debug = Util.isDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri intentUri = intent.getData();
        if (intentUri != null) {
            final Uri uri = Uri.parse(intentUri.toString());
            if (uri != null) {
                 if (DalvikRuntimeArgsService.getActivity() != null) {
                    if (debug) {
                        Log.v(TAG, "LaunchURLActivity :: process ras with uri: " +  uri.toString());
                    }
                    processRuntimeArgs(LAUNCH_URL_KEY, uri.toString());
                } else {
                    Log.v(TAG, "LaunchURLActivity :: Activity doesn't exist, start activity with uri: " +  uri.toString());
                    System.setProperty(LAUNCH_URL_KEY, uri.toString());
                    try {
                        Class<?> clazz = Class.forName("com.gluonhq.helloandroid.MainActivity");
                        intent = new Intent(LaunchURLActivity.this, clazz);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "LaunchURLActivity :: error " + e.getMessage());
                    }
                }
            }
        }
        finish();
    }

    private native void processRuntimeArgs(String key, String value);
}
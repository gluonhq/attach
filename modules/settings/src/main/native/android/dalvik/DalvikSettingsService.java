/*
 * Copyright (c) 2020, Gluon
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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class DalvikSettingsService  {

    private static final String TAG = Util.TAG;

    private final SharedPreferences settings;
    private final boolean debug;

    public DalvikSettingsService(Activity activity) {
        settings = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        debug = Util.isDebug();
    }

    private void store(String key, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
        if (debug) {
            Log.v(TAG, String.format("Updated setting %s = \"%s\"", key, value));
        }
    }

    private void remove(String key) {
        String value = retrieve(key);

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.commit();

        Log.v(TAG, String.format("Removed setting %s = \"%s\"", key, value));
    }

    private String retrieve(String key) {
        return settings.getString(key, null);
    }

}

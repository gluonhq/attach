/*
 * Copyright (c) 2020, 2025, Gluon
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public class DalvikStatusBarService {

    private static final String TAG = Util.TAG;

    private final Activity activity;

    public DalvikStatusBarService(Activity activity) {
        this.activity = activity;
    }

    private void setColor(final int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // < 21
            Log.e(TAG, "setColor is not supported for the current Android version");
            return;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // > 34
            Log.e(TAG, "setColor is not supported for the current Android version. " +
                    "Use setSystemBarsColor instead");
            return;
        }
        if (activity == null) {
            Log.e(TAG, "FXActivity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        if (Util.isDebug()) {
            Log.v(TAG, "Set StatusBar color, value: " + color);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                // make status bar transparent
                window.setStatusBarColor(0x00000000);
                // paint window
                window.setBackgroundDrawable(new ColorDrawable(color));
            }
        });
    }

    private void setSystemBarsColor(final int statusBarColor, final int navigationBarColor) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // < 34
            Log.e(TAG, "setSystemBarsColor is not supported for the current Android version. " +
                    "Use setColor instead");
            return;
        }
        if (activity == null) {
            Log.e(TAG, "FXActivity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        if (Util.isDebug()) {
            Log.v(TAG, "Set SystemBars color, values: " + statusBarColor + ", " + navigationBarColor);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = activity.getWindow();
                View decorView = window.getDecorView();

                // Get insets, before applying the color, as it will reset them
                int top = decorView.getPaddingTop();
                int right = decorView.getPaddingRight();
                int bottom = decorView.getPaddingBottom();
                int left = decorView.getPaddingLeft();

                WindowInsetsController windowInsetsController = decorView.getWindowInsetsController();
                if (windowInsetsController != null) {
                    // background light color requires dark icons, dark light color, white icons
                    int bitset = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;
                    windowInsetsController.setSystemBarsAppearance(getLuma(statusBarColor) > 128 ?
                                    bitset : 0, bitset);
                }

                // Apply colors
                decorView.setBackground(new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{statusBarColor, navigationBarColor}));

                // Restore insets
                decorView.setPadding(left, top, right, bottom);
            }
        });
    }

    private static double getLuma(int color) {
        int r = (color >> 16) & 0xff;
        int g = (color >>  8) & 0xff;
        int b = (color >>  0) & 0xff;
        return 0.2126 * r + 0.7152 * g + 0.0722 * b; // 0 darkest - 255 lightest
    }

}
/*
 * Copyright (c) 2018, 2020, Gluon
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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableException;

import com.gluonhq.helloandroid.ar.ARModel;
import com.gluonhq.helloandroid.ar.ARRenderer;

public class DalvikAugmentedRealityService {

    private static final String TAG = Util.TAG;

    private final boolean debug;
    private boolean debugAR;
    private boolean installRequested;

    private ARModel arModel;

    private enum Availability {
        AR_NOT_SUPPORTED, ARCORE_NOT_INSTALLED, ARCORE_OUTDATED, AR_SUPPORTED
    }

    private final Activity activity;

    public DalvikAugmentedRealityService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
        Util.setLifecycleEventHandler(new LifecycleEventHandler() {
            @Override
            public void lifecycleEvent(String event) {
                if (event != null && !event.isEmpty()) {
                    switch (event) {
                        case "resume":
                            if (installRequested) {
                                if (debug) Log.v(TAG, "ARCore was installed");
                                nativeAugmentedRealityAvailability(checkAR());
                                installRequested = false;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private String checkAR() {
        final Availability availability = checkAvailability();
        if (availability == Availability.ARCORE_NOT_INSTALLED || availability == Availability.ARCORE_OUTDATED) {
            installARCore();
        }
        return availability.name();
    }

    private void showAR() {
        if (!Util.verifyPermissions(Manifest.permission.CAMERA)) {
            Log.e(TAG, "Camera is disabled");
            return;
        }
        ARRenderer renderer = new ARRenderer(activity, arModel, debugAR);
        renderer.render();
    }

    private void enableDebugAR(boolean enable) {
        this.debugAR = enable;
    }

    private void setARModel(String objFilename, String textureFile, double scale) {
        arModel = new ARModel();
        arModel.setObjFilename(objFilename);
        arModel.setTextureFile(textureFile);
        arModel.setScale(scale);
    }

    private final Availability checkAvailability() {
        try {
            final ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(activity);
            Log.v(TAG, "ARCore availability: " + availability.name());
            if (availability.isTransient()) {
                new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (debug) Log.v(TAG, "ARCore transient, checking again");
                        nativeAugmentedRealityAvailability(checkAR());
                    }
                }, 200);
            }
            switch (availability) {
                case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                    if (debug) Log.v(TAG, "ARCore not supported on this device");
                    return Availability.AR_NOT_SUPPORTED;
                case SUPPORTED_APK_TOO_OLD:
                    if (debug) Log.v(TAG, "ARCore supported but apk too old");
                    return Availability.ARCORE_OUTDATED;
                case SUPPORTED_NOT_INSTALLED:
                    if (debug) Log.v(TAG, "ARCore supported but not installed");
                    return Availability.ARCORE_NOT_INSTALLED;
                case UNKNOWN_CHECKING:
                case UNKNOWN_TIMED_OUT:
                case UNKNOWN_ERROR:
                    if (debug) Log.v(TAG, "ARCore not installed. Treated as not supported");
                    return Availability.AR_NOT_SUPPORTED;
                case SUPPORTED_INSTALLED:
                    if (debug) Log.v(TAG, "ARCore supported and installed");
                    return Availability.AR_SUPPORTED;
                default: break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking ARCore availability", e);
        }
        return Availability.AR_NOT_SUPPORTED;
    }

    private boolean installARCore() {
        Log.v(TAG, "Start ARCore installation");
        try {
            ArCoreApk.InstallStatus installStatus =
                    ArCoreApk.getInstance().requestInstall(activity, !installRequested);
            switch (installStatus) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    if (debug) Log.v(TAG, "ARCore installation requested.");
                    return false;
                case INSTALLED:
                    break;
            }
        } catch (UnavailableException e) {
            Log.e(TAG, "ARCore not installed", e);
            return false;
        }
        return true;
    }

    private native void nativeAugmentedRealityAvailability(String result);

}
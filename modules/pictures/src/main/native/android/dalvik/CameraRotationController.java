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
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.camera.view.PreviewView;

/**
 * Tracks display and sensor rotation changes for the camera overlay.
 *
 * Two independent sources are reported to the listener:
 * <ul>
 *   <li>the display rotation, which the {@code PreviewView} follows by itself, and which
 *   serves as fallback for the {@code ImageCapture} target rotation;</li>
 *   <li>the physical device orientation from the sensors, which must drive the
 *   {@code ImageCapture} target rotation so that captured photos are upright even when
 *   the display does not (or can not) rotate.</li>
 * </ul>
 */
final class CameraRotationController {

    interface PreviewViewProvider {
        PreviewView getPreviewView();
    }

    interface Listener {
        /**
         * The display hosting the preview has a new rotation.
         * @param displayRotation one of the {@link Surface} ROTATION_ constants
         */
        void onDisplayRotationChanged(int displayRotation);

        /**
         * The physical device orientation has settled on a new rotation.
         * @param targetRotation one of the {@link Surface} ROTATION_ constants
         */
        void onDeviceOrientationChanged(int targetRotation);
    }

    private static final long ORIENTATION_DEBOUNCE_MS = 150;
    private static final int ROTATION_UNKNOWN = -1;

    private final Activity activity;
    private final DisplayManager displayManager;
    private final PreviewViewProvider previewViewProvider;
    private final Listener listener;
    private final Handler orientationHandler = new Handler(Looper.getMainLooper());

    private boolean started;
    private boolean displayListenerRegistered;
    private OrientationEventListener orientationListener;
    private boolean orientationListenerEnabled;
    private Runnable pendingOrientationUpdate;
    private int pendingDeviceRotation = ROTATION_UNKNOWN;
    // Last rotation delivered to the listener (debounced).
    private int lastDeviceRotation = ROTATION_UNKNOWN;
    // Rotation of the most recent sensor reading (not debounced).
    private int latestDeviceRotation = ROTATION_UNKNOWN;

    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            // no-op
        }

        @Override
        public void onDisplayRemoved(int displayId) {
            // no-op
        }

        @Override
        public void onDisplayChanged(int displayId) {
            Display display = getPreviewDisplay();
            if (display != null && display.getDisplayId() == displayId) {
                notifyDisplayRotation();
            }
        }
    };

    CameraRotationController(Activity activity,
                             DisplayManager displayManager,
                             PreviewViewProvider previewViewProvider,
                             Listener listener) {
        this.activity = activity;
        this.displayManager = displayManager;
        this.previewViewProvider = previewViewProvider;
        this.listener = listener;
    }

    void start() {
        started = true;
        lastDeviceRotation = ROTATION_UNKNOWN;
        latestDeviceRotation = ROTATION_UNKNOWN;
        registerDisplayListener();
        registerOrientationListener();
    }

    void stop() {
        started = false;
        unregisterDisplayListener();
        unregisterOrientationListener();
    }

    /**
     * @return the current rotation of the display hosting the preview, or
     * {@link Surface#ROTATION_0} if the preview is not attached to a display yet
     */
    int getDisplayRotation() {
        Display display = getPreviewDisplay();
        return display != null ? display.getRotation() : Surface.ROTATION_0;
    }

    /**
     * @return the rotation matching the most recent sensor reading, without debouncing,
     * or {@code -1} if the sensors haven't reported yet. Use it when the exact current
     * orientation matters, such as right before a capture.
     */
    int getLatestDeviceRotation() {
        return latestDeviceRotation;
    }

    private Display getPreviewDisplay() {
        PreviewView previewView = previewViewProvider.getPreviewView();
        return previewView != null ? previewView.getDisplay() : null;
    }

    private void notifyDisplayRotation() {
        if (!started) {
            return;
        }
        Display display = getPreviewDisplay();
        if (display != null) {
            listener.onDisplayRotationChanged(display.getRotation());
        }
    }

    private void registerDisplayListener() {
        if (displayListenerRegistered || displayManager == null) {
            return;
        }
        displayManager.registerDisplayListener(displayListener, orientationHandler);
        displayListenerRegistered = true;
    }

    private void unregisterDisplayListener() {
        if (!displayListenerRegistered || displayManager == null) {
            return;
        }
        displayManager.unregisterDisplayListener(displayListener);
        displayListenerRegistered = false;
    }

    private void registerOrientationListener() {
        if (orientationListenerEnabled) {
            return;
        }
        if (orientationListener == null) {
            orientationListener = new OrientationEventListener(activity) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation == ORIENTATION_UNKNOWN || !started) {
                        return;
                    }
                    final int targetRotation = mapOrientationToTargetRotation(orientation);
                    latestDeviceRotation = targetRotation;
                    if (targetRotation == lastDeviceRotation) {
                        // Back in (or still in) the delivered quadrant: nothing to report.
                        cancelPendingOrientationUpdate();
                        return;
                    }
                    if (pendingOrientationUpdate != null && pendingDeviceRotation == targetRotation) {
                        // An update for this quadrant is already scheduled. The sensor jitters
                        // by a degree or more between readings on a hand-held device, so the
                        // timer must not be reset on every reading or it may never fire.
                        return;
                    }
                    cancelPendingOrientationUpdate();
                    pendingDeviceRotation = targetRotation;
                    pendingOrientationUpdate = new Runnable() {
                        @Override
                        public void run() {
                            pendingOrientationUpdate = null;
                            pendingDeviceRotation = ROTATION_UNKNOWN;
                            if (started) {
                                lastDeviceRotation = targetRotation;
                                listener.onDeviceOrientationChanged(targetRotation);
                            }
                        }
                    };
                    orientationHandler.postDelayed(pendingOrientationUpdate, ORIENTATION_DEBOUNCE_MS);
                }
            };
        }
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable();
            orientationListenerEnabled = true;
        }
    }

    private void unregisterOrientationListener() {
        cancelPendingOrientationUpdate();
        if (orientationListener != null && orientationListenerEnabled) {
            orientationListener.disable();
            orientationListenerEnabled = false;
        }
    }

    private void cancelPendingOrientationUpdate() {
        if (pendingOrientationUpdate != null) {
            orientationHandler.removeCallbacks(pendingOrientationUpdate);
            pendingOrientationUpdate = null;
        }
        pendingDeviceRotation = ROTATION_UNKNOWN;
    }

    private int mapOrientationToTargetRotation(int orientation) {
        if (orientation >= 315 || orientation < 45) {
            return Surface.ROTATION_0;
        } else if (orientation < 135) {
            return Surface.ROTATION_270;
        } else if (orientation < 225) {
            return Surface.ROTATION_180;
        } else {
            return Surface.ROTATION_90;
        }
    }
}

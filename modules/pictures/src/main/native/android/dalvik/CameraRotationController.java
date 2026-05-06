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
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.camera.view.PreviewView;

/**
 * Tracks display and sensor rotation changes for the camera overlay.
 * It deals with orientation events, maps them to Surface rotations,
 * and reports stable rotation updates back to the controller.
 */
final class CameraRotationController {

    interface PreviewViewProvider {
        PreviewView getPreviewView();
    }

    interface Listener {
        void onDisplayRotationCommitted();
        void onStableOrientationChanged(int targetRotation);
    }

    private static final long ORIENTATION_DEBOUNCE_MS = 150;

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
            PreviewView previewView = previewViewProvider.getPreviewView();
            if (!started || previewView == null || previewView.getDisplay() == null) {
                return;
            }
            if (previewView.getDisplay().getDisplayId() == displayId) {
                cancelPendingOrientationUpdate();
                listener.onDisplayRotationCommitted();
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
        registerDisplayListener();
        registerOrientationListener();
    }

    void stop() {
        started = false;
        unregisterDisplayListener();
        unregisterOrientationListener();
    }

    int getCurrentTargetRotation() {
        PreviewView previewView = previewViewProvider.getPreviewView();
        if (previewView != null && previewView.getDisplay() != null) {
            return previewView.getDisplay().getRotation();
        }
        return Surface.ROTATION_0;
    }

    private void registerDisplayListener() {
        if (displayListenerRegistered || displayManager == null) {
            return;
        }
        displayManager.registerDisplayListener(displayListener, null);
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
                    cancelPendingOrientationUpdate();
                    pendingOrientationUpdate = new Runnable() {
                        @Override
                        public void run() {
                            pendingOrientationUpdate = null;
                            if (started) {
                                listener.onStableOrientationChanged(targetRotation);
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


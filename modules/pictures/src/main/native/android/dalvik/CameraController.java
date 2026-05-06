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
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.io.File;
import java.util.concurrent.Executor;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Coordinates the Android camera capture flow for the pictures service.
 * It wires together the overlay, rotation handling, Camera binding,
 * capture actions, zoom gestures, and orderly shutdown callbacks.
 */
final class CameraController {

    interface Listener {
        void onCaptured(File targetFile, boolean savePhoto);
        void onCancelled();
    }

    private static final int LENS_BACK = 0;
    private static final int LENS_FRONT = 1;
    private final Activity activity;
    private final String tag;
    private final boolean debug;
    private final Listener listener;
    private final ViewGroup viewGroup;
    private final CameraOverlayView overlayView;
    private final CameraRotationController rotationController;

    private ProcessCameraProvider cameraProvider;
    private Camera boundCamera;
    private ImageCapture imageCapture;
    private CameraLifecycleOwner cameraLifecycleOwner;
    private ScaleGestureDetector zoomGestureDetector;
    private final CameraBackHandler backHandler;

    private boolean cameraVisible;
    private int currentLensFacing = LENS_BACK;

    private Preview previewUseCase;
    private int lastTargetRotation = Surface.ROTATION_0;

    CameraController(Activity activity, String tag, boolean debug, Listener listener) {
        this.activity = activity;
        this.tag = tag;
        this.debug = debug;
        this.listener = listener;
        this.viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        this.overlayView = new CameraOverlayView(activity, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                return zoomGestureDetector != null && zoomGestureDetector.onTouchEvent(event);
            }
        });
        this.rotationController = new CameraRotationController(
                activity,
                (DisplayManager) activity.getSystemService(Activity.DISPLAY_SERVICE),
                new CameraRotationController.PreviewViewProvider() {
                    @Override
                    public PreviewView getPreviewView() {
                        return overlayView.getPreviewViewOrNull();
                    }
                },
                new CameraRotationController.Listener() {
                    @Override
                    public void onDisplayRotationCommitted() {
                        if (cameraVisible) {
                            rebindForRotation();
                        }
                    }

                    @Override
                    public void onStableOrientationChanged(int targetRotation) {
                        if (cameraVisible) {
                            updateTargetRotation(targetRotation, false);
                        }
                    }
                });
        this.backHandler = new CameraBackHandler(activity, new Runnable() {
            @Override
            public void run() {
                closeCamera(true);
            }
        });
        this.lastTargetRotation = resolveTargetRotation();
    }

    boolean start(final boolean savePhoto, final File targetFile) {
        if (targetFile == null) {
            return false;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cameraVisible) {
                        return;
                    }
                    ensureZoomGestureDetector();
                    overlayView.attachTo(viewGroup);
                    rotationController.start();
                    backHandler.attach(overlayView.getRoot());
                    cameraVisible = true;
                    bindCamera(savePhoto, targetFile);
                } catch (Exception e) {
                    Log.e(tag, "Camera startup failed: " + e.getMessage());
                    closeCamera(false);
                    listener.onCancelled();
                }
            }
        });
        return true;
    }


    private void bindCamera(final boolean savePhoto, final File targetFile) {
        if (cameraLifecycleOwner == null) {
            cameraLifecycleOwner = new CameraLifecycleOwner();
        }
        cameraLifecycleOwner.start();

        final Executor mainExecutor = ContextCompat.getMainExecutor(activity);
        overlayView.prepareForBind();
        ensureCameraConfigured();
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(activity);
        providerFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = providerFuture.get();
                    boolean hasBack = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
                    boolean hasFront = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
                    if (!hasBack && !hasFront) {
                        throw new IllegalStateException("No camera available for Camera mode");
                    }
                    if (currentLensFacing == LENS_FRONT && !hasFront) {
                        currentLensFacing = LENS_BACK;
                    }
                    if (currentLensFacing == LENS_BACK && !hasBack) {
                        currentLensFacing = LENS_FRONT;
                    }
                    overlayView.setFlipVisible(hasBack && hasFront);

                    bindCameraUseCases();

                    overlayView.setCancelClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closeCamera(true);
                        }
                    });
                    overlayView.setCaptureClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            captureCameraPhoto(savePhoto, targetFile, mainExecutor);
                        }
                    });
                    overlayView.setFlipClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleLens();
                        }
                    });
                } catch (Exception e) {
                    Log.e(tag, "Camera bind failed: " + e.getMessage());
                    closeCamera(false);
                    listener.onCancelled();
                }
            }
        }, mainExecutor);
    }

    private void ensureZoomGestureDetector() {
        if (zoomGestureDetector != null) {
            return;
        }
        zoomGestureDetector = new ScaleGestureDetector(activity, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return applyPinchZoom(detector.getScaleFactor());
            }
        });
    }

    private void ensureCameraConfigured() {
        try {
            ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig());
        } catch (IllegalStateException alreadyConfigured) {
            // No-op: Camera can only be configured once per process.
        } catch (Throwable t) {
            if (debug) {
                Log.v(tag, "Camera configureInstance skipped: " + t.getMessage());
            }
        }
    }

    private void bindCameraUseCases() {
        int targetRotation = resolveTargetRotation();
        previewUseCase = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(targetRotation)
                .build();
        previewUseCase.setSurfaceProvider(overlayView.getPreviewView().getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(targetRotation)
                .build();
        cameraProvider.unbindAll();
        boundCamera = cameraProvider.bindToLifecycle(cameraLifecycleOwner, getCurrentSelector(), previewUseCase, imageCapture);
        lastTargetRotation = targetRotation;
        if (debug) {
            String lens = currentLensFacing == LENS_FRONT ? "front" : "back";
            Log.v(tag, "Camera bound to " + lens + " lens");
        }
    }

    private CameraSelector getCurrentSelector() {
        return currentLensFacing == LENS_FRONT
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;
    }

    private void toggleLens() {
        if (cameraProvider == null) {
            return;
        }
        int previousLens = currentLensFacing;
        currentLensFacing = currentLensFacing == LENS_BACK ? LENS_FRONT : LENS_BACK;
        try {
            bindCameraUseCases();
        } catch (Exception e) {
            currentLensFacing = previousLens;
            try {
                bindCameraUseCases();
            } catch (Exception retryEx) {
                Log.e(tag, "Camera lens switch failed: " + retryEx.getMessage());
                closeCamera(true);
            }
        }
    }

    private void captureCameraPhoto(final boolean savePhoto, final File targetFile, Executor mainExecutor) {
        if (imageCapture == null) {
            Log.e(tag, "Camera imageCapture is null");
            return;
        }

        ImageCapture.OutputFileOptions opts = new ImageCapture.OutputFileOptions.Builder(targetFile).build();
        imageCapture.takePicture(opts, mainExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                closeCamera(false);
                listener.onCaptured(targetFile, savePhoto);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(tag, "Camera capture failed: " + exception.getMessage());
                closeCamera(true);
            }
        });
    }

    private void closeCamera(final boolean notifyCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!cameraVisible) {
                    return;
                }
                backHandler.detach(overlayView.getRootOrNull());
                rotationController.stop();
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
                if (cameraLifecycleOwner != null) {
                    cameraLifecycleOwner.stop();
                }
                overlayView.detachFrom(viewGroup);
                overlayView.onClosed();
                boundCamera = null;
                previewUseCase = null;
                imageCapture = null;
                cameraVisible = false;
                if (notifyCancel) {
                    listener.onCancelled();
                }
            }
        });
    }

    private int resolveTargetRotation() {
        lastTargetRotation = rotationController.getCurrentTargetRotation();
        return lastTargetRotation;
    }

    private void rebindForRotation() {
        if (cameraProvider == null || cameraLifecycleOwner == null) {
            return;
        }
        int newRotation = resolveTargetRotation();
        if (newRotation == lastTargetRotation) {
            return;
        }
        // No cover for rotation rebinds: in COMPATIBLE (TextureView) mode the surface is
        // NOT destroyed on unbind, so the TextureView freezes on the last frame until the
        // new stream arrives.  Showing a cover would add an unnecessary ~400 ms black delay.
        try {
            bindCameraUseCases();
        } catch (Exception e) {
            Log.w(tag, "Rebind after display rotation failed: " + e.getMessage());
        }
    }

    private void updateTargetRotation(int targetRotation, boolean rebind) {
        if (targetRotation == lastTargetRotation) {
            return;
        }
        lastTargetRotation = targetRotation;
        if (previewUseCase != null) {
            previewUseCase.setTargetRotation(targetRotation);
        }
        if (imageCapture != null) {
            imageCapture.setTargetRotation(targetRotation);
        }
        if (rebind && cameraProvider != null && cameraLifecycleOwner != null) {
            try {
                bindCameraUseCases();
            } catch (Exception e) {
                Log.w(tag, "Rebind after rotation failed: " + e.getMessage());
            }
        }
    }


    private boolean applyPinchZoom(float scaleFactor) {
        if (boundCamera == null) {
            return false;
        }
        ZoomState zoomState = boundCamera.getCameraInfo().getZoomState().getValue();
        if (zoomState == null) {
            return false;
        }
        float nextZoom = zoomState.getZoomRatio() * scaleFactor;
        nextZoom = Math.max(zoomState.getMinZoomRatio(), Math.min(nextZoom, zoomState.getMaxZoomRatio()));
        boundCamera.getCameraControl().setZoomRatio(nextZoom);
        return true;
    }

    private static class CameraLifecycleOwner implements LifecycleOwner {

        private final LifecycleRegistry lifecycleRegistry;

        private CameraLifecycleOwner() {
            lifecycleRegistry = new LifecycleRegistry(this);
            lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        }

        @Override
        @NonNull
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }

        private void start() {
            lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        }

        private void stop() {
            lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        }
    }
}


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
package com.gluonhq.helloandroid.ar;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.transition.Scene;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import com.gluonhq.helloandroid.Util;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;

import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = Util.TAG;

    private final boolean debug = Util.isDebug();
    private final boolean debugAR;
    private final ARModel arModel;
    private final Activity activity;

    private GLSurfaceView surfaceView;
    private BackgroundRenderer backgroundRenderer;
    private final ComplexObjectRenderer virtualObject = new ComplexObjectRenderer();
    private DisplayRotationHelper displayRotationHelper;
    private TapHelper tapHelper;

    private Session session;
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();

    private final float[] anchorMatrix = new float[16];
    private static final float[] DEFAULT_COLOR = new float[]{0f, 0f, 0f, 0f};
    private View originalView;
    private final ArrayList<ColoredAnchor> anchors = new ArrayList<>();

    public ARRenderer(Activity activity, ARModel arModel, boolean debugAR) {
        this.arModel = arModel;
        this.debugAR = debugAR;
        this.activity = activity;
    }

    public void render() {
        setupSurfaceView(activity);
        displayRotationHelper = new DisplayRotationHelper(activity);

        try {
            session = new Session(activity);
            Config config = session.getConfig();
            session.configure(config);

            if (debug) Log.v(TAG, "Session = " + session);
            session.resume();
            backgroundRenderer = new BackgroundRenderer();
            surfaceView.onResume();

            activity.runOnUiThread(() -> {
                displayRotationHelper.onResume();
                View focus = activity.getCurrentFocus();
                if (focus != null) {
                    originalView = focus;
                }
                Scene scene = activity.getContentScene();
                if (debug) Log.v(TAG, "Switch content, focus = " + focus + " and scene = " + scene);
                activity.setContentView(surfaceView,
                        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                tapHelper = new TapHelper(activity);
                surfaceView.setOnTouchListener(tapHelper);
                if (debug) Log.v(TAG, "Switched content to new surfaceview");
            });
            if (debug) Log.v(TAG, "Setcontentview done");
        } catch (UnavailableArcoreNotInstalledException e) {
            Log.e(TAG, "Please install ARCore", e);
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available", e);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglc) {
        if (debug) Log.v(TAG, "Surface created");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        try {
            backgroundRenderer.createOnGlThread(activity);
            planeRenderer.createOnGlThread(activity, "models/trigrid.png");

            pointCloudRenderer.createOnGlThread(activity);

            if (arModel != null && arModel.getObjFilename() != null) {
                if (debug) Log.v(TAG, "Adding ARModel obj");
                virtualObject.createOnGlThread(activity, arModel.getObjFilename(), arModel.getTextureFile());
            }
        } catch (Throwable t) {
            Log.e(TAG, "There was an error creating the surface", t);
            t.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if (debug) Log.v(TAG, "Surface changed, w = " + width + ", h = " + height);
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (session == null) {
            if (debug) Log.v(TAG, "No session is available");
            return;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        displayRotationHelper.updateSessionIfNeeded(session);
        
        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            Frame frame = session.update();
            Camera camera = frame.getCamera();
            handleTap(frame, camera);
            backgroundRenderer.draw(frame);
            
            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            if (debugAR) {
                // Visualize tracked points.
                PointCloud pointCloud = frame.acquirePointCloud();
                pointCloudRenderer.update(pointCloud);
                pointCloudRenderer.draw(viewmtx, projmtx);
                pointCloud.release();

                // Visualize planes.
                planeRenderer.drawPlanes(
                        session.getAllTrackables(Plane.class),
                        camera.getDisplayOrientedPose(), projmtx);
            }

            // Visualize anchors created by touch
            for (ColoredAnchor coloredAnchor : anchors) {
                if (coloredAnchor.anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                coloredAnchor.anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                if (arModel != null && arModel.getObjFilename() != null) {
                    virtualObject.updateModelMatrix(anchorMatrix, (float) arModel.getScale());
                    virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, coloredAnchor.color);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error on draw frame", t);
            t.printStackTrace();
        }

    }

    private void setupSurfaceView(Context context) {
        surfaceView = new GLSurfaceView(context);
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setKeepScreenOn(true);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocus();
        surfaceView.setOnKeyListener((v, k, e) -> {
            if (k == KeyEvent.KEYCODE_BACK) {
                if (debug) Log.v(TAG, "Back pressed, cancelling AR");
                stopAR();
                return true;
            }
            return false;
        });
    }

    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                Trackable trackable = hit.getTrackable();
                if (debug) Log.v(TAG, "hit " + trackable.toString());
                // Creates an anchor if a plane or an oriented point was hit.
                if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode() == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                    // Cap the number of objects created. This avoids overloading both the
                    // rendering system and ARCore.
                    if (anchors.size() >= 20) {
                        anchors.get(0).anchor.detach();
                        anchors.remove(0);
                    }

                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor is created on the Plane to place the 3D model
                    // in the correct position relative both to the world and to the plane.
                    if (debug) Log.v(TAG, "adding colored anchor ");
                    anchors.add(new ColoredAnchor(hit.createAnchor(), DEFAULT_COLOR));
                    break;
                }

            }
        }
    }

    private void stopAR() {
        if (originalView == null) {
            if (debug) Log.e(TAG, "OriginalView was null");
            return;
        }

        activity.runOnUiThread(() -> {
            if (debug) Log.v(TAG, "Put everything on hold");
            onHold();

            if (surfaceView.getParent() != null) {
                ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
            }

            if (originalView.getParent() != null) {
                ((ViewGroup) originalView.getParent()).removeView(originalView);
            }

            if (debug) Log.v(TAG, "Return to original view now");
            activity.setContentView(originalView,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            nativeCancelAR();
        });
    }

    private void onHold() {
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }
    
    private static class ColoredAnchor {

        public final Anchor anchor;
        public final float[] color;

        public ColoredAnchor(Anchor a, float[] color4f) {
            this.anchor = a;
            this.color = color4f;
        }
    }

    private native void nativeCancelAR();
}
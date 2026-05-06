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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

/**
 * Builds and manages the temporary full-screen camera overlay UI.
 * Manages the preview view, startup cover, action buttons, and
 * layout details used while the camera is visible.
 */
final class CameraOverlayView {

    private static final int SMALL_RADIUS = 52;
    private static final int BIG_RADIUS = 74;

    private final Activity activity;
    private final DisplayMetrics displayMetrics;
    private final View.OnTouchListener previewTouchListener;

    private FrameLayout root;
    private PreviewView previewView;
    private View startupCover;
    private Button captureButton;
    private Button cancelButton;
    private ImageButton flipButton;
    private PreviewStreamCoverController previewStreamCoverController;

    private int systemInsetLeft;
    private int systemInsetTop;
    private int systemInsetRight;
    private int systemInsetBottom;

    CameraOverlayView(Activity activity, View.OnTouchListener previewTouchListener) {
        this.activity = activity;
        this.displayMetrics = activity.getResources().getDisplayMetrics();
        this.previewTouchListener = previewTouchListener;
    }

    void attachTo(ViewGroup parent) {
        ensureViews();
        applyFrameLayout();
        if (root.getParent() == null) {
            parent.addView(root);
            ViewCompat.requestApplyInsets(root);
        }
    }

    void detachFrom(ViewGroup parent) {
        if (root != null && root.getParent() == parent) {
            parent.removeView(root);
        }
    }

    FrameLayout getRoot() {
        ensureViews();
        return root;
    }

    FrameLayout getRootOrNull() {
        return root;
    }

    PreviewView getPreviewView() {
        ensureViews();
        return previewView;
    }

    PreviewView getPreviewViewOrNull() {
        return previewView;
    }

    void setCancelClickListener(View.OnClickListener listener) {
        ensureViews();
        cancelButton.setOnClickListener(listener);
    }

    void setCaptureClickListener(View.OnClickListener listener) {
        ensureViews();
        captureButton.setOnClickListener(listener);
    }

    void setFlipClickListener(View.OnClickListener listener) {
        ensureViews();
        flipButton.setOnClickListener(listener);
    }

    void setFlipVisible(boolean visible) {
        ensureViews();
        flipButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    void prepareForBind() {
        ensureViews();
        previewStreamCoverController.prepareForBind();
        previewStreamCoverController.attach();
    }

    void onClosed() {
        if (previewStreamCoverController != null) {
            previewStreamCoverController.detach();
        }
    }

    private void ensureViews() {
        if (root != null) {
            return;
        }

        root = new FrameLayout(activity);
        root.setBackgroundColor(0xFF111111);

        previewView = new PreviewView(activity);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        previewView.setBackgroundColor(Color.BLACK);
        previewView.setOnTouchListener(previewTouchListener);
        root.addView(previewView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        startupCover = new View(activity);
        startupCover.setBackgroundColor(Color.BLACK);
        root.addView(startupCover, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        previewStreamCoverController = new PreviewStreamCoverController(previewView, startupCover);

        FrameLayout controlsContainer = new FrameLayout(activity);
        FrameLayout.LayoutParams controlsLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        controlsContainer.setLayoutParams(controlsLp);

        cancelButton = createSymbolButton("\u2715", SMALL_RADIUS, 0xCC666666, Color.TRANSPARENT, 0, 24f, Color.WHITE);
        int radius = dpToPx(SMALL_RADIUS);
        FrameLayout.LayoutParams cancelLp = new FrameLayout.LayoutParams(
                radius, radius, Gravity.START | Gravity.BOTTOM);
        controlsContainer.addView(cancelButton, cancelLp);

        flipButton = createIconButton(R.drawable.ic_flip_camera, SMALL_RADIUS, 40, 0xCC666666);
        FrameLayout.LayoutParams flipLp = new FrameLayout.LayoutParams(
                radius, radius, Gravity.END | Gravity.BOTTOM);
        controlsContainer.addView(flipButton, flipLp);

        captureButton = createSymbolButton("", BIG_RADIUS, 0xFFDDDDDD, 0xAA777777, 2, 0f, Color.TRANSPARENT);
        FrameLayout.LayoutParams captureLp = new FrameLayout.LayoutParams(
                dpToPx(BIG_RADIUS), dpToPx(BIG_RADIUS), Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        controlsContainer.addView(captureButton, captureLp);

        root.addView(controlsContainer);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            systemInsetLeft = insets.getSystemWindowInsetLeft();
            systemInsetTop = insets.getSystemWindowInsetTop();
            systemInsetRight = insets.getSystemWindowInsetRight();
            systemInsetBottom = insets.getSystemWindowInsetBottom();
            applyInsetsAwareLayout();
            return insets;
        });
        applyInsetsAwareLayout();
    }

    private void applyFrameLayout() {
        if (root == null) {
            return;
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        lp.setMargins(0, 0, 0, 0);
        root.setLayoutParams(lp);
        applyInsetsAwareLayout();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * displayMetrics.density);
    }

    private Button createSymbolButton(String symbol, int diameterDp, int fillColor, int strokeColor,
                                      int strokeWidthDp, float textSizeSp, int textColor) {
        Button button = new Button(activity);
        button.setIncludeFontPadding(false);
        button.setPadding(0, 0, 0, 0);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setText(symbol);
        button.setTextColor(textColor);
        if (textSizeSp > 0f) {
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        }
        button.setBackground(createCircularButtonBackground(fillColor, strokeColor, strokeWidthDp));

        int diameterPx = dpToPx(diameterDp);
        button.setWidth(diameterPx);
        button.setHeight(diameterPx);
        return button;
    }

    private ImageButton createIconButton(int drawableResId, int diameterDp, int iconSizeDp, int fillColor) {
        ImageButton button = new ImageButton(activity);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setAdjustViewBounds(false);
        int diameterPx = dpToPx(diameterDp);
        int iconSizePx = Math.min(dpToPx(iconSizeDp), diameterPx);
        int insetPx = Math.max(0, (diameterPx - iconSizePx) / 2);
        button.setPadding(insetPx, insetPx, insetPx, insetPx);
        button.setBackground(createCircularButtonBackground(fillColor, Color.TRANSPARENT, 0));

        Drawable icon = ContextCompat.getDrawable(activity, drawableResId);
        if (icon != null) {
            Drawable wrapped = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(wrapped, Color.WHITE);
            button.setImageDrawable(wrapped);
        }
        return button;
    }

    private Drawable createCircularButtonBackground(int fillColor, int strokeColor, int strokeWidthDp) {
        GradientDrawable content = new GradientDrawable();
        content.setShape(GradientDrawable.OVAL);
        content.setColor(fillColor);
        content.setStroke(dpToPx(strokeWidthDp), strokeColor);

        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setColor(Color.WHITE);

        return new RippleDrawable(ColorStateList.valueOf(0x4DFFFFFF), content, mask);
    }

    private void applyInsetsAwareLayout() {
        if (root == null || cancelButton == null || captureButton == null) {
            return;
        }

        int margin = dpToPx(16);
        int sideMargin = margin;
        int bottomMargin = margin + systemInsetBottom;
        int topMargin = margin;

        FrameLayout.LayoutParams cancelLp = (FrameLayout.LayoutParams) cancelButton.getLayoutParams();
        cancelLp.setMargins(sideMargin + systemInsetLeft, topMargin, sideMargin, bottomMargin);
        cancelButton.setLayoutParams(cancelLp);

        FrameLayout.LayoutParams captureLp = (FrameLayout.LayoutParams) captureButton.getLayoutParams();
        captureLp.setMargins(sideMargin + systemInsetLeft, topMargin, sideMargin + systemInsetRight, bottomMargin);
        captureButton.setLayoutParams(captureLp);

        if (flipButton != null) {
            FrameLayout.LayoutParams flipLp = (FrameLayout.LayoutParams) flipButton.getLayoutParams();
            flipLp.setMargins(sideMargin, topMargin, sideMargin + systemInsetRight, bottomMargin);
            flipButton.setLayoutParams(flipLp);
        }

        root.setPadding(systemInsetLeft, systemInsetTop, systemInsetRight, 0);
    }
}


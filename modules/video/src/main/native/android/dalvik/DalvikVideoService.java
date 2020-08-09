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
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class DalvikVideoService implements TextureView.SurfaceTextureListener, MediaController.MediaPlayerControl {

    private static final String TAG = Util.TAG;

    enum Status {
        UNKNOWN,
        READY,
        PAUSED,
        PLAYING,
        STOPPED,
        DISPOSED
    }

    private final Activity activity;
    private final ViewGroup viewGroup;
    private String[] playlist;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private String videoName;
    private final FrameLayout frameLayout;
    private final TextureView textureView;

    private boolean showing;
    private boolean ready;
    private boolean looping;
    private boolean controlsVisible;

    private final AudioManager audioManager;
    private final int maxVolume;
    private int preMuteVolume = 0;
    private int currentVolume = 0;

    private Status status = Status.UNKNOWN;
    private boolean fullScreen;
    private int currentIndex;

    private int alignH;
    private int alignV;
    private double topPadding = 0;
    private double rightPadding = 0;
    private double bottomPadding = 0;
    private double leftPadding = 0;
    private double mediaHeight = 0;
    private double mediaWidth = 0;

    private boolean isVideo;
    private FileInputStream fis;

    private final boolean debug;

    public DalvikVideoService(final Activity activity) {
        this.activity = activity;
        viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        debug = Util.isDebug();

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC‌​);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        frameLayout = new FrameLayout(activity);
        textureView = new TextureView(activity) {

            private boolean scaling;
            private ScaleGestureDetector mScaleDetector;
            {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScaleDetector = new ScaleGestureDetector(activity, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public void onScaleEnd(ScaleGestureDetector detector) {
                                super.onScaleEnd(detector);
                                if (debug) {
                                    Log.v(TAG, String.format("Pinch detected with scale %f", detector.getScaleFactor()));
                                }
                                if (fullScreen && detector.getScaleFactor() < 1.0) {
                                    updateFullScreen(false);
                                } else if (!fullScreen && detector.getScaleFactor() > 1.0) {
                                    updateFullScreen(true);
                                }
                                scaling = false;
                            }

                            @Override
                            public boolean onScaleBegin(ScaleGestureDetector detector) {
                                scaling = true;
                                return true;
                            }
                        });
                    }
                });

                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!scaling) {
                            if (controlsVisible && isVideo && mediaController != null && !mediaController.isShowing()) {
                                mediaController.show();
                            }
                        }
                    }
                });
            }

            @Override
            protected void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                resizeRelocateVideo();
            }

            @Override
            public boolean dispatchKeyEvent(final KeyEvent ke) {
                dispatchVolume(ke);
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                requestFocus();
                mScaleDetector.onTouchEvent(event);
                return super.onTouchEvent(event);
            }
        };

        textureView.setFocusable(true);
        textureView.setFocusableInTouchMode(true);
        textureView.setSurfaceTextureListener(this);

        frameLayout.addView(textureView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
    }

    private void setPlaylist(String[] playlist) {
        this.playlist = playlist;
        if (playlist == null || playlist.length == 0) {
            hide();
        } else if (videoName != null) {
            // update current Index if any change happens
            boolean contains = false;
            int index = -1;
            for (int i = 0; i < playlist.length; i++) {
                if (videoName.equals(playlist[i])) {
                    index = i;
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                // current video was removed, restart from first item
                if (currentIndex == 0) {
                    updateCurrentIndex(-1);
                }
                // update index
                updateCurrentIndex(0);
            } else {
                if (index != currentIndex) {
                    // update index to new position in playlist
                    currentIndex = index;
                    nativeCurrentIndex(currentIndex);
                }
            }
        }
    }

    private void show() {
        if (playlist == null || playlist.length == 0) {
            Log.e(TAG, "The playlist is empty");
            return;
        }

        if (showing) {
            if (debug) {
                Log.v(TAG, "Video layer was already added");
            }
            return;
        }

        videoName = playlist[currentIndex];
        updateReady(false);
        showing = true;

        if (prepareMedia()) {
            if (isVideo) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (debug) {
                            Log.v(TAG, "Showing video layer");
                        }
                        viewGroup.addView(frameLayout);
                        textureView.requestFocus();
                    }
                });
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (debug) {
                            Log.v(TAG, "Adding audio layer");
                        }
                        viewGroup.addView(frameLayout, 0);
                        textureView.requestFocus();
                    }
                });
            }
        } else {
            if (debug) {
                Log.v(TAG, "Invalid media file found, trying the next one");
            }
            showing = false;
            updateCurrentIndex(currentIndex + 1);
        }
    }

    private void play() {
        if (playlist == null || playlist.length == 0) {
            Log.e(TAG, "The playlist is empty");
            return;
        }
        if (status == Status.STOPPED || status == Status.DISPOSED) {
            // rewind
            updateStatus(Status.UNKNOWN);
            internalHide();
            updateCurrentIndex(0);
        }

        if (!ready) {
            if (!showing) {
                show();
            }
        } else if (mediaPlayer != null) {
            if (debug) {
                Log.v(TAG, "Video play");
            }
            updateStatus(Status.PLAYING);
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            if (debug) {
                Log.v(TAG, "Video pause");
            }
            updateStatus(Status.PAUSED);
            mediaPlayer.pause();
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            if (debug) {
                Log.v(TAG, "Video stop");
            }
            mediaPlayer.stop();
            updateStatus(Status.STOPPED);
        }
    }

    private void hide() {
        internalHide();
        updateStatus(Status.DISPOSED);
    }

    private void setPosition(String alignmentH, String alignmentV, double topPadding, double rightPadding, double bottomPadding, double leftPadding) {
        if ("LEFT".equals(alignmentH)) {
            alignH = -1;
        } else if ("RIGHT".equals(alignmentH)) {
            alignH = 1;
        } else {
            alignH = 0;
        }
        if ("TOP".equals(alignmentV)) {
            alignV = -1;
        } else if ("BOTTOM".equals(alignmentV)) {
            alignV = 1;
        } else {
            alignV = 0;
        }
        this.topPadding = topPadding;
        this.rightPadding  = rightPadding;
        this.bottomPadding = bottomPadding;
        this.leftPadding = leftPadding;
    }

    private void setLooping(boolean looping) {
        this.looping = looping;
    }

    private void setControlsVisible(boolean controlsVisible) {
        this.controlsVisible = controlsVisible;
    }

    private void setFullScreen(boolean fullScreen) {
        if (this.fullScreen != fullScreen) {
            this.fullScreen = fullScreen;
            updateSystemUI(fullScreen);
        }
    }

    private void setCurrentIndex(int index) {
        if (index < 0 || (playlist != null && index >= playlist.length)) {
            if (debug) {
                Log.e(TAG, "Wrong item value");
            }
            return;
        }
        updateCurrentIndex(index);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int i, int i1) {
        if (debug) {
            Log.v(TAG, "Adding surface to Media player");
        }
        Surface surface = new Surface(st);
        mediaPlayer.setSurface(surface);

        setupMedia();
    }

    private void setupMedia() {
        resizeRelocateVideo();

        try {
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException ex) {
            Log.e(TAG, "Media player error: " + ex.getMessage());
            updateStatus(Status.UNKNOWN);
            mediaPlayer.release();
            mediaPlayer = null;
            return;
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                updateCurrentIndex(currentIndex + 1);
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (debug) {
                    Log.v(TAG, "Media player prepared and ready");
                }
                if (controlsVisible && isVideo) {
                    mediaController = new MediaController(activity);
                    mediaController.setMediaPlayer(DalvikVideoService.this);
                    mediaController.setAnchorView(textureView);

                    new Handler(activity.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaController != null) {
                                mediaController.setEnabled(true);
                                mediaController.show();
                            }
                        }
                    });
                }

                updateStatus(Status.READY);
                updateReady(true);
            }
        });
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st, int i, int i1) { }
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st) { return true; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture st) { }

    @Override public void start() { play(); }
    @Override public int getDuration() { return mediaPlayer.getDuration(); }
    @Override public int getCurrentPosition() { return mediaPlayer.getCurrentPosition(); }
    @Override public void seekTo(int i) { mediaPlayer.seekTo(i); }
    @Override public boolean isPlaying() { return mediaPlayer.isPlaying(); }
    @Override public int getBufferPercentage() { return 0; }
    @Override public boolean canPause() { return true; }
    @Override public boolean canSeekBackward() { return true; }
    @Override public boolean canSeekForward() { return true; }
    @Override public int getAudioSessionId() { return 0; }

    private boolean prepareMedia() {
        try {
            if (debug) {
                Log.v(TAG, String.format("Creating new MediaPlayer for %s", videoName));
            }
            mediaPlayer = new MediaPlayer();

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            if (Patterns.WEB_URL.matcher(videoName).matches()) {
                metaRetriever.setDataSource(videoName, new HashMap<String, String>());
                mediaPlayer.setDataSource(activity, Uri.parse(videoName));
                if (debug) {
                    Log.v(TAG, String.format("Video file from URL: %s", Uri.parse(videoName).toString()));
                }
            } else {
                FileDescriptor fd = getFileDescriptor(videoName);
                if (fd != null) {
                    if (debug) {
                        Log.v(TAG, String.format("Got Video file from Resources: %s", fd.valid() ? "valid" : "invalid"));
                    }
                    metaRetriever.setDataSource(fd);
                    mediaPlayer.setDataSource(fd);
                    if (fis != null) {
                        fis.close();
                    }
                } else {
                    Log.e(TAG, String.format("Invalid video file: %s", videoName));
                    updateStatus(Status.UNKNOWN);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    return false;
                }
            }
            isVideo = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null;
            mediaHeight = parse(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            mediaWidth = parse(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            if (debug) {
                if (isVideo) {
                    Log.v(TAG, String.format("Video size: %f x %f", mediaWidth, mediaHeight));
                } else {
                    Log.v(TAG, "Audio file");
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error loading content", e);
            updateStatus(Status.UNKNOWN);
            mediaPlayer.release();
            mediaPlayer = null;
            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "Error loading content", e);
            updateStatus(Status.UNKNOWN);
            mediaPlayer.release();
            mediaPlayer = null;
            return false;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error loading content", e);
            updateStatus(Status.UNKNOWN);
            mediaPlayer.release();
            mediaPlayer = null;
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error loading content", e);
            updateStatus(Status.UNKNOWN);
            mediaPlayer.release();
            mediaPlayer = null;
            return false;
        }
        return true;
    }

    private void nextMedia(int index) {
        if (debug) {
            Log.v(TAG, "Hiding current video file");
        }
        internalHide();
        if (0 <= index && (playlist != null && index < playlist.length)) {
            if (debug) {
                Log.v(TAG, String.format("Showing video file [%d/%d]", currentIndex + 1, playlist.length));
            }
            play();
        } else {
            if (debug) {
                Log.v(TAG, "Disposing media player");
            }
            if (fullScreen) {
                updateFullScreen(false);
            }
            updateStatus(Status.DISPOSED);
        }
    }

    private void internalHide() {
        if (mediaPlayer != null) {
            if (debug) {
                Log.v(TAG, "Media Player release");
            }
            if (mediaController != null) {
                mediaController.hide();
                mediaController = null;
            }
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        showing = false;
        if (frameLayout != null /* && isVideo  */) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewGroup.removeView(frameLayout);
                }
            });
        }
        updateReady(false);
    }

    private void resizeRelocateVideo() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        final float scaleFactor = (fullScreen ? 0 : 1) * displayMetrics.scaledDensity;
        final double maxW = displayMetrics.widthPixels - (leftPadding + rightPadding) * scaleFactor;
        final double maxH = displayMetrics.heightPixels - (topPadding + bottomPadding) * scaleFactor;
        final double screenFactor = maxW / maxH;
        int w, h;
        if (isVideo) {
            final double mediaFactor = mediaWidth / mediaHeight;
            if (mediaFactor > screenFactor) {
                w = (int) maxW;
                h = (int) (maxW / mediaFactor);
            } else {
                w = (int) (maxH * mediaFactor);
                h = (int) maxH;
            }
        } else {
            w = (int) maxW;
            h = (int) Math.min(maxH, 300);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h, Gravity.CENTER);
        if (!fullScreen) {
            if (alignV == -1) {
                lp.gravity = Gravity.TOP;
            } else if (alignV == 1) {
                lp.gravity = Gravity.BOTTOM;
            } else {
                lp.gravity = Gravity.CENTER;
            }
            if (alignH == -1) {
                lp.gravity += Gravity.LEFT;
            } else if (alignH == 1) {
                lp.gravity += Gravity.RIGHT;
            } else if (alignV != 0) {
                lp.gravity += Gravity.CENTER_HORIZONTAL;
            }
        }
        lp.setMargins((int) (leftPadding * scaleFactor), (int) (topPadding * scaleFactor),
                (int) (rightPadding * scaleFactor), (int) (bottomPadding * scaleFactor));

        textureView.setLayoutParams(lp);
        if (debug) {
            Log.v(TAG, String.format("Media margins: %d %d %d %d", lp.topMargin, lp.rightMargin, lp.bottomMargin, lp.leftMargin));
        }

        frameLayout.setBackgroundColor(fullScreen ? Color.BLACK : Color.TRANSPARENT);

        if (fullScreen && !isVideo) {
            if (debug) {
                Log.v(TAG, "Audio file doesn't allow full screen mode");
            }
            updateFullScreen(false);
        }
    }

    private void updateSystemUI(final boolean fullScreen) {
        if (mediaPlayer == null) {
            if (debug) {
                Log.v(TAG, "No media player found");
            }
            updateFullScreen(false);
            return;
        }
        if (fullScreen && !isVideo) {
            if (debug) {
                Log.v(TAG, "Audio file doesn't allow full screen mode");
            }
            updateFullScreen(false);
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fullScreen) {
                    if (debug) {
                        Log.v(TAG, "Entering full screen mode");
                    }
                    activity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {
                    if (debug) {
                        Log.v(TAG, "Exiting full screen mode");
                    }
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
                resizeRelocateVideo();
            }
        });
    }

    private void setScreenOn(final boolean on) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = activity.getWindow();
                if (on) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    private double parse(String s) {
        if (s == null || s.isEmpty()) {
            return 0d;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException nfe) {}
        return 0d;
    }

    private void updateFullScreen(boolean fullScreen) {
        if (this.fullScreen != fullScreen) {
            setFullScreen(fullScreen);
            nativeFullScreen(fullScreen);
        }
    }

    private void updateReady(boolean ready) {
        if (this.ready != ready) {
            this.ready = ready;
            if (ready) {
                if (debug) {
                    Log.v(TAG, String.format("Video start playing [%d/%d]: %s", currentIndex + 1, playlist.length, videoName));
                }
                play();
            }
        }
    }

    private void updateStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            setScreenOn(status == Status.PLAYING);
            nativeStatus(status.ordinal());
        }
    }

    private void updateCurrentIndex(int currentIndex) {
        if (this.currentIndex != currentIndex) {
            this.currentIndex = currentIndex;
            pause();
            if (0 <= currentIndex && (playlist != null && currentIndex < playlist.length)) {
                nextMedia(currentIndex);
            } else if (looping) {
                updateCurrentIndex(0); // rewind
            } else {
                nextMedia(-1); // stop
            }
            nativeCurrentIndex(currentIndex);
        }
    }

    private void dispatchVolume(final KeyEvent ke) {
        if (ke.getAction() == KeyEvent.ACTION_DOWN) {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            switch (ke.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            (currentVolume + 1) <= maxVolume ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            (currentVolume - 1) >= 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                    break;
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                    if (currentVolume > 0) {
                        preMuteVolume = currentVolume;
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
                    } else {
                        preMuteVolume = 0;
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, preMuteVolume, AudioManager.FLAG_SHOW_UI);
                    }
                    break;
                default: break;
            }
        }
    }

    private FileDescriptor getFileDescriptor(String filePath) {
        if (debug) {
            Log.v(TAG, String.format("Finding file descriptor for video file: %s", filePath));
        }
        try {
            File videoFile = new File(filePath);
            if (!videoFile.exists()) {
                Log.e(TAG, String.format("Video file: %s doesn't exist", videoFile));
                return null;
            }
            fis = new FileInputStream(videoFile);
            return fis.getFD();
        } catch (IOException ex) {
            Log.e(TAG, "Error getting file descriptor", ex);
        }
        return null;
    }

    private native void nativeStatus(int status);
    private native void nativeFullScreen(boolean fullScreen);
    private native void nativeCurrentIndex(int currentIndex);
}
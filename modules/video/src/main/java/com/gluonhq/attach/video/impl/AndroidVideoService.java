/*
 * Copyright (c) 2017, 2020, Gluon
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
package com.gluonhq.attach.video.impl;

import com.gluonhq.attach.video.Status;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AndroidVideoService extends DefaultVideoService {

    private static final ReadOnlyObjectWrapper<Status> STATUS = new ReadOnlyObjectWrapper<>();
    private static final BooleanProperty FULL_SCREEN = new SimpleBooleanProperty();
    private static final IntegerProperty CURRENT_INDEX = new SimpleIntegerProperty();
    static {
        System.loadLibrary("video");
    }

    public AndroidVideoService() {
        super();

        playlist.addListener((Observable o) -> {
            List<String> list = new ArrayList<>();
            for (String s : playlist) {
                if (checkFileInResources(s)) {
                    File videoFile = getFileFromAssets(s);
                    list.add(videoFile.getAbsolutePath());
                } else {
                    list.add(s);
                }
            }
            setVideoPlaylist(list.toArray(new String[0]));
        });

        FULL_SCREEN.addListener((obs, ov, nv) -> setFullScreenMode(nv));
        CURRENT_INDEX.addListener((obs, ov, nv) -> currentIndex(nv.intValue()));
    }

    @Override
    public void show() {
        showVideo();
    }

    @Override
    public void play() {
        playVideo();
    }

    @Override
    public void stop() {
        stopVideo();
    }

    @Override
    public void pause() {
        pauseVideo();
    }

    @Override
    public void hide() {
        hideVideo();
    }

    @Override
    public void setPosition(Pos alignment, double topPadding, double rightPadding, double bottomPadding, double leftPadding) {
        setPosition(alignment.getHpos().name(), alignment.getVpos().name(), topPadding, rightPadding, bottomPadding, leftPadding);
    }

    @Override
    public void setLooping(boolean looping) {
        looping(looping);
    }

    @Override
    public void setControlsVisible(boolean controlsVisible) {
        controlsVisible(controlsVisible);
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        FULL_SCREEN.set(fullScreen);
    }

    @Override
    public BooleanProperty fullScreenProperty() {
        return FULL_SCREEN;
    }

    @Override
    public ReadOnlyObjectProperty<Status> statusProperty() {
        return STATUS.getReadOnlyProperty();
    }

    @Override
    public void setCurrentIndex(int index) {
        CURRENT_INDEX.set(index);
    }

    @Override
    public IntegerProperty currentIndexProperty() {
        return CURRENT_INDEX;
    }

    // native
    private native void setVideoPlaylist(String[] playlist);
    private native void showVideo();
    private native void playVideo();
    private native void stopVideo();
    private native void pauseVideo();
    private native void hideVideo();
    private native void looping(boolean looping);
    private native void controlsVisible(boolean controlsVisible);
    private native void currentIndex(int currentIndex);
    private native void setFullScreenMode(boolean fullScreen);
    private native void setPosition(String alignmentH, String alignmentV, double topPadding, double rightPadding, double bottomPadding, double leftPadding);

    // callbacks
    private static void updateStatus(int value) {
        Status s;
        switch (value) {
            case 0: s = Status.UNKNOWN; break;
            case 1: s = Status.READY; break;
            case 2: s = Status.PAUSED; break;
            case 3: s = Status.PLAYING; break;
            case 4: s = Status.STOPPED; break;
            case 5: s = Status.DISPOSED; break;
            default: s = Status.UNKNOWN;
        }
        if (STATUS.get() != s) {
            Platform.runLater(() -> STATUS.set(s));
        }
    }

    private static void updateFullScreen(boolean value) {
        if (FULL_SCREEN.get() != value) {
            Platform.runLater(() -> FULL_SCREEN.set(value));
        }
    }

    private static void updateCurrentIndex(int index) {
        if (CURRENT_INDEX.get() != index) {
            Platform.runLater(() -> CURRENT_INDEX.set(index));
        }
    }
}

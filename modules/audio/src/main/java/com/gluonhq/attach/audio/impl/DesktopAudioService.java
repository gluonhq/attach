/*
 * Copyright (c) 2020 Gluon
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
package com.gluonhq.attach.audio.impl;

import com.gluonhq.attach.audio.Audio;
import com.gluonhq.attach.audio.AudioService;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class DesktopAudioService implements AudioService {

    @Override
    public Audio loadSound(URL url) {
        return new DesktopSound(new AudioClip(url.toExternalForm()));
    }

    @Override
    public Audio loadMusic(URL url) {
        return new DesktopMusic(new MediaPlayer(new Media(url.toExternalForm())));
    }

    private static final class DesktopSound implements Audio {

        private final AudioClip clip;

        public DesktopSound(AudioClip clip) {
            this.clip = clip;
        }

        @Override
        public void setLooping(boolean looping) {
            clip.setCycleCount(looping ? Integer.MAX_VALUE : 1);
        }

        @Override
        public void setVolume(double volume) {
            clip.setVolume(volume);
        }

        @Override
        public void setOnFinished(Runnable action) {
            // TODO: ?
        }

        @Override
        public void play() {
            clip.play();
        }

        @Override
        public void pause() {
            clip.stop();
        }

        @Override
        public void stop() {
            clip.stop();
        }

        @Override
        public void dispose() {
            // TODO: ?
        }

        @Override
        public boolean isDisposed() {
            return false;
        }
    }

    private static final class DesktopMusic implements Audio {

        private boolean isDisposed = false;
        private final MediaPlayer mediaPlayer;

        public DesktopMusic(MediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public void setLooping(boolean looping) {
            mediaPlayer.setCycleCount(looping ? Integer.MAX_VALUE : 1);
        }

        @Override
        public void setVolume(double volume) {
            mediaPlayer.setVolume(volume);
        }

        @Override
        public void setOnFinished(Runnable action) {
            mediaPlayer.setOnEndOfMedia(action);
        }

        @Override
        public void play() {
            mediaPlayer.play();
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public void stop() {
            mediaPlayer.stop();
        }

        @Override
        public void dispose() {
            isDisposed = true;
            mediaPlayer.dispose();
        }

        @Override
        public boolean isDisposed() {
            return isDisposed;
        }
    }
}

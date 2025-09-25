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
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


/**
 * @author Dominik Pospisil (dominik.pospisil@gmail.com)
 */
public class DesktopAudioService implements AudioService {

    private static final Logger LOG = Logger.getLogger(DesktopAudioService.class.getName());


    private static class DesktopAudioClip implements Audio {

        private AudioClip clip;

        public DesktopAudioClip(AudioClip clip) {
            this.clip = clip;
        }

        @Override
        public void setLooping(boolean looping) {
            clip.setCycleCount(looping ? AudioClip.INDEFINITE : 1);
        }

        @Override
        public void setVolume(double volume) {
            clip.setVolume(volume);
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
            clip = null;
        }

        @Override
        public boolean isDisposed() {
            return clip == null;
        }
    }

    private static class DesktopMedia implements Audio {

        private Media media;
        private MediaPlayer player;

        public DesktopMedia(Media media) {
            this.media = media;
            this.player = new MediaPlayer(media);
        }

        @Override
        public void setLooping(boolean looping) {
            player.setCycleCount(looping ? MediaPlayer.INDEFINITE : 1);
        }

        @Override
        public void setVolume(double volume) {
            player.setVolume(volume);
        }

        @Override
        public void play() {
            player.play();
        }

        @Override
        public void pause() {
            player.pause();
        }

        @Override
        public void stop() {
            player.stop();
        }

        @Override
        public void dispose() {
            media = null;
            player = null;
        }

        @Override
        public boolean isDisposed() {
            return media == null;
        }

    }





    @Override
    public Optional<Audio> loadSound(URL url) {
        return loadAudioImpl(url);
    }

    @Override
    public Optional<Audio> loadMusic(URL url) {
        return loadMediaImpl(url);
    }

    private Optional<Audio> loadAudioImpl(URL url) {

        try {
            AudioClip clip = new AudioClip(url.toExternalForm());
            Optional<Audio> audio = Optional.of(new DesktopAudioClip(clip));
            return audio;
        } catch (Exception e) {
            LOG.warning("Error loading audio:" + e.toString());
            return Optional.empty();
        }
    }

    private Optional<Audio> loadMediaImpl(URL url) {

        try {
            Media media = new Media(url.toExternalForm());
            Optional<Audio> audio = Optional.of(new DesktopMedia(media));
            return audio;
        } catch (Exception e) {
            LOG.warning("Error loading music:" + e.toString());
            return Optional.empty();
        }
    }

}

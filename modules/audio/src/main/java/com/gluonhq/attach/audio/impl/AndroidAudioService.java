/*
 * Copyright (c) 2020, 2023, Gluon
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
import com.gluonhq.attach.storage.StorageService;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class AndroidAudioService implements AudioService {

    private static final Logger LOG = Logger.getLogger(AndroidAudioService.class.getName());

    static {
        System.loadLibrary("audio");
    }

    private File privateStorage;

    @Override
    public Optional<Audio> loadSound(URL url) {
        return loadAudioImpl(url, AndroidAudioService::loadSoundImpl);
    }

    @Override
    public Optional<Audio> loadMusic(URL url) {
        return loadAudioImpl(url, AndroidAudioService::loadMusicImpl);
    }

    private Optional<Audio> loadAudioImpl(URL url, Function<String, Integer> loaderFunc) {
        if (url == null)
            return Optional.empty();

        try {
            String fullName = copyToPrivateStorageIfNeeded(url);

            if (fullName.isEmpty())
                return Optional.empty();

            int audioId = loaderFunc.apply(fullName);

            if (audioId == -1)
                return Optional.empty();

            return Optional.of(new AndroidAudio(audioId));

        } catch (Exception e) {
            LOG.fine("Error during loading audio: " + e);
            return Optional.empty();
        }
    }

    /**
     * Copy file (if it doesn't exist) to private storage.
     * Throws an exception if any error occurred during copying.
     *
     * @param url where the file is
     * @return full path to file in private storage where it was copied
     */
    private String copyToPrivateStorageIfNeeded(URL url) throws Exception {
        if (privateStorage == null) {
            privateStorage = StorageService.create()
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));
        }

        String extForm = url.toExternalForm();
        String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);

        Path file = privateStorage.toPath()
                .resolve("assets")
                .resolve("audio")
                .resolve(fileName);

        if (!Files.exists(file)) {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            try (InputStream input = url.openStream()) {
                Files.copy(input, file);
            }
        }

        return file.toAbsolutePath().toString();
    }

    // All native calls are executed in a background thread to not hold the caller thread (which is probably the UI
    // thread). This prevents games using many sounds to be slowed down by audio calls.
    private final static ScheduledExecutorService nativeExecutor = Executors.newSingleThreadScheduledExecutor();

    private static class AndroidAudio implements Audio {

        private final int id;
        private boolean isDisposed = false;
        private boolean pendingPlay = false; // flag used by play() to alleviate the Audio flow in extreme situations
        private boolean skipPause = true; // flag used to skip unnecessary calls to pause(), because calling pause() before play() prevents the music to be played (Android issue)
        private boolean skipStop = true;  // flag used to skip unnecessary calls to stop(), because calling stop() before play() prevents the music to be played (Android issue)

        AndroidAudio(int id) {
            this.id = id;
        }

        @Override
        public void setLooping(boolean looping) {
            if (isDisposed)
                return;

            nativeExecutor.execute(() -> AndroidAudioService.setLooping(id, looping));
        }

        @Override
        public void setVolume(double volume) {
            if (isDisposed)
                return;

            nativeExecutor.execute(() -> AndroidAudioService.setVolume(id, volume));
        }

        @Override
        public void play() {
            // We set pendingPlay to true before the native play() call, and then back to false after that call.
            // In extreme situations (like observed with SpaceFX with many simultaneous explosions sounds), it can
            // happen that the game calls play() again even before the previous call has been executed. In that case,
            // we just drop that second call, as it doesn't make sense to start the same sound twice so closely. And
            // most important, this improves the performance (the game was noticeably slower when the native iOS sound
            // system was not alleviate in this way).

            if (isDisposed || pendingPlay)
                return;

            pendingPlay = true;
            nativeExecutor.execute(() -> {
                AndroidAudioService.play(id);
                pendingPlay = false;
            });
            skipPause = skipStop = false;
        }

        @Override
        public void pause() {
            if (isDisposed || skipPause)
                return;

            nativeExecutor.execute(() -> AndroidAudioService.pause(id));
            skipPause = true;
        }

        @Override
        public void stop() {
            if (isDisposed || skipStop)
                return;

            nativeExecutor.execute(() -> AndroidAudioService.stop(id));
            skipPause = skipStop = true;
        }

        @Override
        public void dispose() {
            if (isDisposed)
                return;

            isDisposed = true;
            nativeExecutor.execute(() -> AndroidAudioService.dispose(id));
        }

        @Override
        public boolean isDisposed() {
            return isDisposed;
        }
    }

    private native static int loadSoundImpl(String fullName);
    private native static int loadMusicImpl(String fullName);

    private native static void setLooping(int audioId, boolean looping);
    private native static void setVolume(int audioId, double volume);
    private native static void play(int audioId);
    private native static void pause(int audioId);
    private native static void stop(int audioId);
    private native static void dispose(int audioId);
}

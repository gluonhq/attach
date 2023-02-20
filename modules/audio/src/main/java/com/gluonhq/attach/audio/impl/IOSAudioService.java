/*
 * Copyright (c) 2023, Gluon
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
import java.util.logging.Logger;

/**
 * @author Bruno Salmon
 */
public class IOSAudioService implements AudioService {

    private static final Logger LOG = Logger.getLogger(IOSAudioService.class.getName());

    static {
        System.loadLibrary("Audio");
        initAudio();
    }

    @Override
    public Optional<Audio> loadSound(URL url) {
        return loadAudio(url, false);
    }

    @Override
    public Optional<Audio> loadMusic(URL url) {
        return loadAudio(url, true);
    }

    private Optional<Audio> loadAudio(URL url, boolean music) {
        if (url != null) {
            try {
                String fileLocation = copyToPrivateStorageIfNeeded(url);
                if (!fileLocation.isEmpty()) {
                    int id = loadSoundImpl(fileLocation, music);
                    if (id >= 0) // A negative value means the sound couldn't be loaded by iOS
                        return Optional.of(new IOSAudio(id));
                }
            } catch (Exception e) {
                LOG.fine("Error while loading audio " + url + ": " + e);
            }
        }
        return Optional.empty();
    }


    // native
    private static native void initAudio(); // A call to this method is necessary before using the other native methods
    private static native int loadSoundImpl(String url, boolean music);
    private static native void setLooping(int id, boolean looping);
    private static native void setVolume(int id, double volume);
    private static native void play(int id);
    private static native void pause(int id);
    private static native void stop(int id);
    private static native void dispose(int id);

    private File privateStorage;

    /**
     * Copy file (if it doesn't exist) to private storage.
     * Throws an exception if any error occurred during copying.
     *
     * @param url where the file is
     * @return full path to file in private storage where it was copied
     */
    private String copyToPrivateStorageIfNeeded(URL url) throws Exception {
        String extForm = url.toExternalForm();
        // iOS only supports audio local files, it doesn't support http streaming for example. So when it's not a local
        // file, we need to copy it first to the private storage before being able to play it.
        if (!extForm.startsWith("file:")) { // Note: this also applies for "resource:" as GraalVM resources can't be directly accessed by iOS

            String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);

            if (privateStorage == null) {
                privateStorage = StorageService.create()
                        .flatMap(StorageService::getPrivateStorage)
                        .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));
            }

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

            extForm = file.toUri().toString();
        }

        return extForm;
    }

    // All native calls are executed in a background thread to not hold the caller thread (which is probably the UI
    // thread). In particular the call to the play() method has been observed to take up to 50ms which is
    // noticeable in apps like games with 60 FPS (where time between frames = 16ms).
    private final static ScheduledExecutorService nativeExecutor = Executors.newSingleThreadScheduledExecutor();

    private static class IOSAudio implements Audio {

        private final int id; // identifier of the Audio to be passed to the native service
        private boolean pendingPlay; // flag used by play() to alleviate the Audio flow in extreme situations
        private boolean disposed; // true after calling dispose(), making this instance unusable anymore

        public IOSAudio(int id) {
            this.id = id;
        }

        @Override
        public void setLooping(boolean looping) {
            if (!disposed) {
                nativeExecutor.execute(() -> IOSAudioService.setLooping(id, looping));
            }
        }

        @Override
        public void setVolume(double volume) {
            if (!disposed) {
                nativeExecutor.execute(() -> IOSAudioService.setVolume(id, volume));
            }
        }

        @Override
        public void play() {
            // We set pendingPlay to true before the native play() call, and then back to false after that call.
            // In extreme situations (like observed with SpaceFX with many simultaneous explosions sounds), it can
            // happen that the game calls play() again even before the previous call has been executed. In that case,
            // we just drop that second call, as it doesn't make sense to start the same sound twice so closely. And
            // most important, this improves the performance (the game was noticeably slower when the native iOS sound
            // system was not alleviate in this way).
            if (!disposed && !pendingPlay) {
                pendingPlay = true;
                nativeExecutor.execute(() -> {
                    IOSAudioService.play(id);
                    pendingPlay = false;
                });
            }
        }

        @Override
        public void pause() {
            if (!disposed) {
                nativeExecutor.execute(() -> IOSAudioService.pause(id));
            }
        }

        @Override
        public void stop() {
            if (!disposed) {
                nativeExecutor.execute(() -> IOSAudioService.stop(id));
            }
        }

        @Override
        public void dispose() {
            if (!disposed) {
                nativeExecutor.execute(() -> IOSAudioService.dispose(id));
                disposed = true;
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}

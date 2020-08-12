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
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Optional;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class AndroidAudioService implements AudioService {

    static {
        System.loadLibrary("audio");
    }

    private File privateStorage;

    @Override
    public Optional<Audio> loadSound(URL url) {
        String fullName = prepareFile(url, "sounds/");

        if (fullName.isEmpty())
            return Optional.empty();

        int audioId = loadSoundImpl(fullName);

        if (audioId == -1)
            return Optional.empty();

        return Optional.of(new AndroidAudio(audioId));
    }

    @Override
    public Optional<Audio> loadMusic(URL url) {
        String fullName = prepareFile(url, "music/");

        if (fullName.isEmpty())
            return Optional.empty();

        int audioId = loadMusicImpl(fullName);

        if (audioId == -1)
            return Optional.empty();

        return Optional.of(new AndroidAudio(audioId));
    }

    /**
     * Copy file (if it doesn't exist) to private storage.
     *
     * @param url where the file is
     * @param subDirName subdirectory name where the file will go
     * @return file name in private storage if file was copied or if it already exists at destination,
     * or empty String if there are any errors during copying
     */
    private String prepareFile(URL url, String subDirName) {
        if (privateStorage == null) {
            privateStorage = setUpDirectories();
        }

        String extForm = url.toExternalForm();
        String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);
        String fullName = privateStorage.getAbsolutePath() + "/assets/" + subDirName + fileName;

        File outputFile = new File(fullName);

        if (!outputFile.exists()) {
            try {
                copyFile(url, outputFile);
            } catch (Exception e) {

                // TODO:
                e.printStackTrace();

                return "";
            }
        }

        return fullName;
    }

    private File setUpDirectories() {
        File storage = Services.get(StorageService.class)
                .flatMap(service -> service.getPrivateStorage())
                .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));

        File assetsDir = new File(storage, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdir();
        }

        File musicDir = new File(assetsDir, "music");
        if (!musicDir.exists()) {
            musicDir.mkdir();
        }

        File soundsDir = new File(assetsDir, "sounds");
        if (!soundsDir.exists()) {
            soundsDir.mkdir();
        }

        return storage;
    }

    private void copyFile(URL url, File outputFile) throws Exception {
        try (InputStream input = url.openStream()) {
            if (input == null) {
                throw new RuntimeException("Internal copy failed: input stream for " + url + " is null");
            }

            try (OutputStream output = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            }
        }
    }

    private static class AndroidAudio implements Audio {

        private boolean isDisposed = false;
        private final int id;

        AndroidAudio(int id) {
            this.id = id;
        }

        @Override
        public void setLooping(boolean looping) {
            if (isDisposed)
                return;

            AndroidAudioService.setLooping(id, looping);
        }

        @Override
        public void setVolume(double volume) {
            if (isDisposed)
                return;

            AndroidAudioService.setVolume(id, volume);
        }

        @Override
        public void setOnFinished(Runnable action) {
            // TODO:
        }

        @Override
        public void play() {
            if (isDisposed)
                return;

            AndroidAudioService.play(id);
        }

        @Override
        public void pause() {
            if (isDisposed)
                return;

            AndroidAudioService.pause(id);
        }

        @Override
        public void stop() {
            if (isDisposed)
                return;

            AndroidAudioService.stop(id);
        }

        @Override
        public void dispose() {
            if (isDisposed)
                return;

            isDisposed = true;
            AndroidAudioService.dispose(id);
        }

        @Override
        public boolean isDisposed() {
            return isDisposed;
        }
    }

    // native
    private native static int loadSoundImpl(String fullName);
    private native static int loadMusicImpl(String fullName);

    private native static void setLooping(int audioId, boolean looping);
    private native static void setVolume(int audioId, double volume);
    private native static void play(int audioId);
    private native static void pause(int audioId);
    private native static void stop(int audioId);
    private native static void dispose(int audioId);
}

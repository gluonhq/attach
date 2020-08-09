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

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.gluonhq.attach.audio.Audio;
import com.gluonhq.attach.storage.StorageService;

import java.io.*;
import java.net.URL;

public class DalvikAudioService {

    private SoundPool pool;

    private File privateStorage;

    public DalvikAudioService() {

    }

    private Audio loadSoundImpl(String url) {
        File file = getFile(new URL(url), "sounds/");

        if (pool == null)
            pool = createPool();

        FileInputStream stream = new FileInputStream(file);

        int soundID = pool.load(stream.getFD(), 0, file.length(), 1);

        stream.close();

        return new AndroidSound(pool, soundID);
    }

    private Audio loadMusicImpl(String url) {
        File file = getFile(new URL(url), "music/");

        FileInputStream stream = new FileInputStream(file);

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(stream.getFD());
        mediaPlayer.prepare();

        stream.close();

        return new AndroidMusic(mediaPlayer);
    }

    private File getFile(URL url, String subDirName) {
        if (privateStorage == null) {
            privateStorage = setUpDirectories();
        }

        String extForm = url.toExternalForm();
        String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);
        String fullName = privateStorage.getAbsolutePath() + "/assets/" + subDirName + fileName;

        File outputFile = new File(fullName);

        if (!outputFile.exists()) {
            copyFile(url, outputFile);
        }

        return outputFile;
    }

    private File setUpDirectories() {
        File storage = StorageService.create()
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

    private SoundPool createPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        return new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // this is arbitrary, but it should be a reasonable amount
                .setMaxStreams(5)
                .build();
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
}
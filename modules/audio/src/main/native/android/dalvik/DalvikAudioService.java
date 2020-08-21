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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class DalvikAudioService {

    private DalvikAudio[] cache = new DalvikAudio[10];

    private SoundPool pool = null;

    public DalvikAudioService() {

    }

    /**
     * @param fullName file name in private storage
     * @return audio id or -1 if failure
     */
    private int loadSoundImpl(String fullName) {
        if (pool == null)
            pool = createPool();

        File file = new File(fullName);

        try (FileInputStream stream = new FileInputStream(file)) {

            int soundID = pool.load(stream.getFD(), 0, file.length(), 1);

            DalvikSound sound = new DalvikSound(pool, soundID);

            int index = getFreeSlot();
            cache[index] = sound;

            return index;

        } catch (Exception e) {
            // TODO: exception
            return -1;
        }
    }

    /**
     * @param fullName file name in private storage
     * @return audio id or -1 if failure
     */
    private int loadMusicImpl(String fullName) {
        File file = new File(fullName);

        try (FileInputStream stream = new FileInputStream(file)) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(stream.getFD());
            mediaPlayer.prepare();

            DalvikMusic music = new DalvikMusic(mediaPlayer);

            int index = getFreeSlot();
            cache[index] = music;

            return index;

        } catch (Exception e) {
            // TODO: exception
            return -1;
        }
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

    private int getFreeSlot() {
        for (int i = 0; i < cache.length; i++) {
            if (cache[i] == null) {
                return i;
            }
        }

        return growCache();
    }

    /**
     * Grows internal cache.
     *
     * @return free slot
     */
    private int growCache() {
        int oldLength = cache.length;

        cache = Arrays.copyOf(cache, oldLength + 10);

        // last slot of old cache is oldLength - 1, so next slot is oldLength - 1 + 1
        return oldLength;
    }

    private void setLooping(int audioId, boolean looping) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            audio.setLooping(looping);
        }
    }

    private void setVolume(int audioId, double volume) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            audio.setVolume(volume);
        }
    }

    private void play(int audioId) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            audio.play();
        }
    }

    private void pause(int audioId) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            audio.pause();
        }
    }

    private void stop(int audioId) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            audio.stop();
        }
    }

    private void dispose(int audioId) {
        DalvikAudio audio = getAudio(audioId);
        if (audio != null) {
            cache[audioId] = null;
            audio.dispose();
        }
    }

    private DalvikAudio getAudio(int audioId) {
        if (audioId >= 0 && audioId < cache.length)
            return cache[audioId];

        return null;
    }
}
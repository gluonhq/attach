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
package com.gluonhq.helloandroid;

import android.media.SoundPool;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class DalvikSound implements DalvikAudio {

    private final SoundPool pool;
    private final int soundID;

    private boolean looping = false;
    private float volume = 1.0f;
    private int lastStreamID = 0;

    private boolean isDisposed = false;

    public DalvikSound(SoundPool pool, int soundID) {
        this.pool = pool;
        this.soundID = soundID;
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    @Override
    public void setVolume(double volume) {
        this.volume = (float) volume;
    }

    @Override
    public void play() {
        lastStreamID = pool.play(soundID, volume, volume, 1, looping ? -1 : 0, 1);
    }

    @Override
    public void pause() {
        pool.pause(lastStreamID);
    }

    @Override
    public void stop() {
        pool.stop(lastStreamID);
    }

    @Override
    public void dispose() {
        isDisposed = true;
        pool.unload(soundID);
    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}
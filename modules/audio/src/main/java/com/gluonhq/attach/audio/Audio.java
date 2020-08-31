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
package com.gluonhq.attach.audio;

/**
 * An abstraction of a native sound or music object.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 * @since 4.0.9
 */
public interface Audio {

    /**
     * Set the audio to loop or play once.
     *
     * @param looping flag to control whether audio is to be played in looping mode
     */
    void setLooping(boolean looping);

    /**
     * Set the volume with which to play this audio.
     *
     * @param volume range [0..1]
     */
    void setVolume(double volume);

    /**
     * Play (or resume if paused) this audio.
     */
    void play();

    /**
     * Pause this audio. Next call to {@link #play()} will resume playing the audio.
     */
    void pause();

    /**
     * Stop playing this audio. Next call to {@link #play()} will start playing the audio from the beginning.
     */
    void stop();

    /**
     * Releases (native) resources associated with this audio.
     * No other methods should be called on this audio after this method returns.
     */
    void dispose();

    /**
     * If this method returns true, no other methods should be called on this audio.
     *
     * @return whether resources associated with this audio were released
     */
    boolean isDisposed();
}
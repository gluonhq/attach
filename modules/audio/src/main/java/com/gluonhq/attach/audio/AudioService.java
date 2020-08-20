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
package com.gluonhq.attach.audio;

import com.gluonhq.attach.util.Services;

import java.net.URL;
import java.util.Optional;

/**
 * The audio service provides access to loading native audio (sound and music) objects.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 * @since 4.0.9
 */
public interface AudioService {

    /**
     * @return an instance of {@link AudioService}.
     */
    static Optional<AudioService> create() {
        return Services.get(AudioService.class);
    }

    /**
     * Load a (short) sound object (typically ".wav") from a given URL. 
     * This call will block until the audio is fully loaded.
     * You may wish to load the audio on a non-JavaFX thread.
     *
     * @param url where the sound file is
     * @return optional containing the sound file or empty if any errors occurred during loading
     */
    Optional<Audio> loadSound(URL url);

    /**
     * Load a (long) music object (typically ".mp3") from a given URL.
     * This call will block until the audio is fully loaded.
     * You may wish to load the audio on a non-JavaFX thread.
     *
     * @param url where the music file is
     * @return optional containing the music file or empty if any errors occurred during loading
     */
    Optional<Audio> loadMusic(URL url);
}

/*
 * Copyright (c) 2016, 2019 Gluon
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
package com.gluonhq.attach.vibration;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The vibration service enables access to the vibration functionality present
 * on most devices. It allows alerting the user when notification through sound
 * is inappropriate or unavailable.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code VibrationService.create().ifPresent(service -> {
 *      service.vibrate();
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permission <code>android.permission.VIBRATE</code> needs to be added.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.VIBRATE"/>
 *    ...
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface VibrationService {

    /**
     * Returns an instance of {@link VibrationService}.
     * @return An instance of {@link VibrationService}.
     */
    static Optional<VibrationService> create() {
        return Services.get(VibrationService.class);
    }

    /**
     * Vibrates the device with the default pattern and duration
     */
    void vibrate();

    /**
     * Vibrates the device with the given pattern, which represents the number of
     * milliseconds to turn the vibration on for, followed by how long it should be off for.
     *
     * <p>In the simple case of a single vibration, a call of {@code vibrate(2000)} will result in the
     * vibration running for 2 seconds before stopping.</p>
     *
     * <p>If a pattern is desired, multiple durations can be provided, where each odd duration
     * represents a vibration duration, and each even duration represents an amount of time to wait.
     * For example, a call of {@code vibrate(1000, 1000, 2000, 2000, 3000)} will result in the
     * following pattern:</p>
     *
     * <ul>
     *     <li>Vibrate for 1 second</li>
     *     <li>Wait for 1 second</li>
     *     <li>Vibrate for 2 seconds</li>
     *     <li>Wait for 2 seconds</li>
     *     <li>Vibrate for 3 seconds</li>
     * </ul>
     *
     * Note: the availability of this functionality is platform-restricted, and at present only Android supports it. 
     * Calling this method on iOS will result in the same vibration as calling {@link #vibrate()}
     *
     * @param pattern The pattern of durations to play the vibration for (with wait periods in between).
     */
    void vibrate(long... pattern);
}

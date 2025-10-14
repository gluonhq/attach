/*
 * Copyright (c) 2025 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
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

package com.gluonhq.attach.ads;

/**
 * Used for callbacks from the native code.
 */
public class Callback {

    /**
     * The name of the callback class.
     */
    private final String name;

    /**
     * The actual callback.
     */
    private final Object callback;

    /**
     * Constructs a new instance and throws an exception if the provided
     * callback is not a known callback.
     *
     * @param callback the callback to use
     */
    public Callback(Object callback) {
        String name = null;

        if (callback instanceof AdListener)
            name = AdListener.class.getSimpleName();
        if (callback instanceof InterstitialAdLoadCallback)
            name = InterstitialAdLoadCallback.class.getSimpleName();
        if (callback instanceof RewardedAdLoadCallback)
            name = RewardedAdLoadCallback.class.getSimpleName();
        if (callback instanceof OnUserEarnedRewardListener)
            name = OnUserEarnedRewardListener.class.getSimpleName();
        if (callback instanceof FullScreenContentCallback)
            name = FullScreenContentCallback.class.getSimpleName();

        if (name == null) {
            throw new IllegalArgumentException("Invalid callback specified");
        }

        this.name = name;
        this.callback = callback;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the callback.
     *
     * @return the callback
     */
    public Object getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return "{name=" + name + ", callback=" + callback + "}";
    }
}

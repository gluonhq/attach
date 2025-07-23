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
 * A listener for receiving notifications during the lifecycle
 * of an ad.
 */
public abstract class AdListener {

    /**
     * Invokes callbacks that are received from the native code.
     */
    static class Handler implements AdRegistry.CallbackHandler {

        /**
         * Match the callback from native code to a method defined
         * in this class.
         *
         * @param ad the ad the callback is intended for
         * @param callback the class the callback method belongs to
         * @param callbackMethod the callback method to call
         * @param params the params for the callback method
         */
        @Override
        public void handle(Ad<?> ad, Object callback, String callbackMethod, String[] params) {
            AdListener c = (AdListener) callback;

            switch (callbackMethod) {
                case "onAdClicked": c.onAdClicked(); break;
                case "onAdClosed": c.onAdClosed(); break;
                case "onAdFailedToLoad": c.onAdFailedToLoad(); break;
                case "onAdImpression": c.onAdImpression(); break;
                case "onAdLoaded": c.onAdLoaded(); break;
                case "onAdOpened": c.onAdOpened(); break;
                case "onAdSwipeGestureClicked": c.onAdSwipeGestureClicked(); break;
            }
        }
    }

    /**
     * Called when a click is recorded for an ad.
     */
    public void onAdClicked() {
    }

    /**
     * Called when the user is about to return to the application
     * after clicking on an ad.
     */
    public void onAdClosed() {
    }

    /**
     * Called when an ad request failed.
     */
    public void onAdFailedToLoad() {
    }

    /**
     * Called when an impression is recorded for an ad.
     */
    public void onAdImpression() {
    }

    /**
     * Called when an ad is received.
     */
    public void onAdLoaded() {
    }

    /**
     * Called when an ad opens an overlay that covers the screen.
     */
    public void onAdOpened() {
    }

    /**
     * Called when a swipe gesture on an ad is recorded as a click.
     */
    public void onAdSwipeGestureClicked() {
    }
}

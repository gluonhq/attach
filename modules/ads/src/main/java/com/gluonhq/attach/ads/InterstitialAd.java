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
 * Shows the user a short interstitial ad.
 */
public class InterstitialAd extends Ad<InterstitialAd.Service> {

    /**
     * Defines all methods needed for the ads service.
     */
    public interface Service {

        /**
         * Shows the ad after it is loaded.
         *
         * @param ad the ad to show
         */
        void show(InterstitialAd ad);

        /**
         * Sets the callback for the ad when it is opened, clicked, etc.
         *
         * @param ad the ad to set the callback for
         * @param callback the callback used for the callbacks
         */
        void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback);
    }

    /**
     * Constructs a new ad with the specified id and service.
     *
     * @param id the unique id of this ad
     * @param service the service used for this ad
     */
    public InterstitialAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Shows the ad after it is loaded.
     */
    public void show() {
        service.show(this);
    }

    /**
     * Sets the callback for the ad when it is opened, clicked, etc.
     *
     * @param callback the callback used for the callbacks
     */
    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        service.setFullScreenContentCallback(this, callback);
    }
}

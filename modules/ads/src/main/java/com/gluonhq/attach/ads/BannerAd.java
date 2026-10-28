/*
 * Copyright (c) 2026, Gluon
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
 * An ad used to display banner ads. The ad size and ad unit ID must be set prior to calling loadAd.
 */
public class BannerAd extends Ad<BannerAd.Service> {

    /**
     * The ad unit ID used for testing.
     */
    public static final String TEST_AD_UNIT_ID = "test/banner";

    /**
     * {@inheritDoc}
     */
    public BannerAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Loads an ad.
     *
     * @param adRequest the ad request
     */
    public void load(AdRequest adRequest) {
        service.load(this, adRequest);
    }

    /**
     * Shows the ad.
     */
    public void show() {
        service.show(this);
    }

    /**
     * Hides the ad.
     */
    public void hide() {
        service.hide(this);
    }

    /**
     * Sets the layout of the ad.
     *
     * @param layout the layout
     */
    public void setAdLayout(Layout layout) {
        service.setAdLayout(this, layout);
    }

    /**
     * Sets the size of the ad.
     *
     * @param size the size of the ad
     */
    public void setAdSize(Size size) {
        service.setAdSize(this, size);
    }

    /**
     * Sets the ad unit ID.
     *
     * @param adUnitId the ad unit ID
     */
    public void setAdUnitId(String adUnitId) {
        service.setAdUnitId(this, adUnitId);
    }

    /**
     * Sets an AdListener for this ad view.
     *
     * @param listener the listener
     */
    public void setAdListener(AdListener listener) {
        service.setAdListener(this, listener);
    }

    /**
     * The layout of a banner ad.
     */
    public enum Layout {

        /**
         * Positions the banner ad at the top center.
         */
        TOP,

        /**
         * Positions the banner ad at the bottom center.
         */
        BOTTOM,
    }

    /**
     * The size of a banner ad.
     */
    public enum Size {

        /**
         * Mobile Marketing Association (MMA) banner ad size (320x50 density-independent pixels).
         */
        BANNER,

        /**
         * Interactive Advertising Bureau (IAB) full banner ad size (468x60 density-independent pixels).
         */
        FULL_BANNER,

        /**
         * Large banner ad size (320x100 density-independent pixels).
         */
        LARGE_BANNER,

        /**
         * Interactive Advertising Bureau (IAB) leaderboard ad size (728x90 density-independent pixels).
         */
        LEADERBOARD,

        /**
         * Interactive Advertising Bureau (IAB) medium rectangle ad size (300x250 density-independent pixels).
         */
        MEDIUM_RECTANGLE,

        /**
         * IAB wide skyscraper ad size (160x600 density-independent pixels).
         */
        WIDE_SKYSCRAPER,

        /**
         * A dynamically sized banner that matches its parent's width and expands/contracts its height to match the ad's
         * content after loading completes.
         */
        FLUID,

        /**
         * An invalid AdSize that will cause the ad request to fail immediately.
         */
        INVALID,
    }

    public interface Service extends Ad.Service {

        void load(BannerAd ad, AdRequest adRequest);

        void show(BannerAd ad);

        void hide(BannerAd ad);

        void setAdLayout(BannerAd ad, Layout layout);

        void setAdSize(BannerAd ad, Size size);

        void setAdUnitId(BannerAd ad, String adUnitId);

        void setAdListener(BannerAd ad, AdListener listener);
    }
}

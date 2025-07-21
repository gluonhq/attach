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

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The service used for providing the ability to load and show ads.
 */
public interface AdsService {

    /**
     * Test ad unit id for banner ads.
     */
    String BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    /**
     * Test ad unit id for interstitial ads.
     */
    String INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    /**
     * Test ad unit id for interstitial video ads.
     */
    String INTERSTITIAL_VIDEO_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/8691691433";

    /**
     * Test ad unit id for rewarded ads.
     */
    String REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    /**
     * Creates a new {@code AdsService}.
     *
     * @return the {@code AdsService} instance
     */
    static Optional<AdsService> create() {
        return Services.get(AdsService.class);
    }

    /**
     * Initialize the MobileAds SDK.
     *
     * @param listener the listener that is called on completion
     */
    void initialize(OnInitializationCompleteListener listener);

    /**
     * Creates a new banner ad.
     *
     * @return the new banner ad
     */
    BannerAd newBannerAd();

    /**
     * Loads an interstitial ad.
     *
     * @param adUnitId the ad unit id of the ad
     * @param adRequest the ad request to use
     * @param callback the callback when loaded
     */
    void loadInterstitialAd(String adUnitId, AdRequest adRequest, InterstitialAdLoadCallback callback);

    /**
     * Loads a rewarded ad.
     *
     * @param adUnitId the ad unit id of the ad
     * @param adRequest the ad request to use
     * @param callback the callback when loaded
     */
    void loadRewardedAd(String adUnitId, AdRequest adRequest, RewardedAdLoadCallback callback);

    /**
     * Set the request configuration that is used for all ad requests.
     *
     * @param requestConfiguration the configuration to use for all
     *                             ad requests
     */
    void setRequestConfiguration(RequestConfiguration requestConfiguration);
}

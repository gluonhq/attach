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

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The ads service provides the ability to show ads with the Google Mobile Ads SDK on Android and iOS.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code AdsService.create().ifPresent(service -> {
 *      service.initialize(() -> {
 *          BannerAd ad = service.newBannerAd();
 *
 *          ad.setAdUnitId(BannerAd.TEST_AD_UNIT_ID);
 *          ad.setAdLayout(BannerAd.Layout.BOTTOM);
 *          ad.setAdSize(BannerAd.Size.BANNER);
 *          ad.load(new AdRequest.Builder().build());
 *          ad.show();
 *      });
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: you need to set the com.google.android.gms.ads.APPLICATION_ID inside your AndroidManifest.xml file</p>
 * <p><b>iOS Configuration</b>: you need to set the GADApplicationIdentifier inside your Partial-Info.plist file</p>
 *
 * @since 4.0.24
 */
public interface AdsService {

    /**
     * Creates an AdsService.
     *
     * @return the created ad service
     */
    static Optional<AdsService> create() {
        return Services.get(AdsService.class);
    }

    /**
     * Initializes the Google Mobile Ads SDK. Call this method as early as possible after the app launches to reduce
     * latency on the session's first ad request. If this method is not called, the first ad request automatically
     * initializes the Google Mobile Ads SDK.
     *
     * @param listener a callback to be invoked upon initialization completion
     */
    void initialize(OnInitializationCompleteListener listener);

    /**
     * Constructs a new BannerAd.
     *
     * @return the constructed banner ad
     */
    BannerAd newBannerAd();

    /**
     * Loads an InterstitialAd.
     *
     * @param adUnitId the ad unit ID
     * @param adRequest an ad request with targeting information
     * @param callback a callback to be invoked when an interstitial ad finishes loading
     */
    void loadInterstitialAd(String adUnitId, AdRequest adRequest, InterstitialAdLoadCallback callback);

    /**
     * Loads a RewardedAd.
     *
     * @param adUnitId the ad unit ID
     * @param adRequest an ad request with targeting information
     * @param callback a callback to be invoked when a rewarded ad finishes loading
     */
    void loadRewardedAd(String adUnitId, AdRequest adRequest, RewardedAdLoadCallback callback);

    /**
     * Sets the global RequestConfiguration that will be used for every AdRequest during the app's session.
     *
     * @param requestConfiguration the request configuration
     */
    void setRequestConfiguration(RequestConfiguration requestConfiguration);
}

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
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
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

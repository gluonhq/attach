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

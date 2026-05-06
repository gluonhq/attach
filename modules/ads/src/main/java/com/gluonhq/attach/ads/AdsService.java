package com.gluonhq.attach.ads;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

public interface AdsService {

    String BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    String INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    String INTERSTITIAL_VIDEO_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/8691691433";

    String REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    static Optional<AdsService> create() {
        return Services.get(AdsService.class);
    }

    void initialize(OnInitializationCompleteListener listener);

    BannerAd newBannerAd();

    void loadInterstitialAd(String adUnitId, AdRequest adRequest, InterstitialAdLoadCallback callback);

    void loadRewardedAd(String adUnitId, AdRequest adRequest, RewardedAdLoadCallback callback);

    void setRequestConfiguration(RequestConfiguration requestConfiguration);
}

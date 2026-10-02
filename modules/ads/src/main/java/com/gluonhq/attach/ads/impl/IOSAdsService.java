package com.gluonhq.attach.ads.impl;

import com.gluonhq.attach.ads.BannerAd;
import com.gluonhq.attach.ads.InterstitialAd;
import com.gluonhq.attach.ads.RewardedAd;

public class IOSAdsService extends DefaultAdsService {

    static {
        System.loadLibrary("Ads");
        initAds();
    }

    @Override
    protected String getAdUnitId(String adUnitId) {
        switch (adUnitId) {
            case BannerAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/2435281174";
            case InterstitialAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/4411468910";
            case RewardedAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/1712485313";
            default: return adUnitId;
        }
    }

    private static native void initAds();

    @Override
    protected native void nativeInitialize();

    @Override
    protected native void nativeSetRequestConfiguration(String ageRestrictedTreatment, String maxAdContentRating, String[] testDeviceIds);

    @Override
    protected native void nativeRemoveAd(long id);

    @Override
    protected native void nativeBannerAdNew(long id);

    @Override
    protected native void nativeBannerAdLoad(long id);

    @Override
    protected native void nativeBannerAdShow(long id);

    @Override
    protected native void nativeBannerAdHide(long id);

    @Override
    protected native void nativeBannerAdSetAdLayout(long id, String layout);

    @Override
    protected native void nativeBannerAdSetAdSize(long id, String size);

    @Override
    protected native void nativeBannerAdSetAdUnitId(long id, String adUnitId);

    @Override
    protected native void nativeInterstitialAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeInterstitialAdShow(long id);

    @Override
    protected native void nativeRewardedAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeRewardedAdShow(long id);
}

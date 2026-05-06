package com.gluonhq.attach.ads.impl;

public class AndroidAdsService extends DefaultAdsService {

    static {
        System.loadLibrary("ads");
    }

    @Override
    protected native void nativeInitialize();

    @Override
    protected native void nativeSetRequestConfiguration(int tagForChildDirectedTreatment, int tagForUnderAgeOfConsent, String maxAdContentRating, String[] testDeviceIds);

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
    protected native void nativeBannerAdSetLayout(long id, String layout);

    @Override
    protected native void nativeBannerAdSetAdSize(long id, String size);

    @Override
    protected native void nativeBannerAdSetAdUnitId(long id, String adUnitId);

    @Override
    protected native void nativeBannerAdSetAdListener(long id);

    @Override
    protected native void nativeInterstitialAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeInterstitialAdShow(long id);

    @Override
    protected native void nativeRewardedAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeRewardedAdShow(long id);
}

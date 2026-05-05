package com.gluonhq.attach.ads.impl;

import com.gluonhq.attach.ads.Ad;
import com.gluonhq.attach.ads.AdListener;
import com.gluonhq.attach.ads.AdRegistry;
import com.gluonhq.attach.ads.AdRequest;
import com.gluonhq.attach.ads.AdsService;
import com.gluonhq.attach.ads.BannerAd;
import com.gluonhq.attach.ads.FullScreenContentCallback;
import com.gluonhq.attach.ads.InterstitialAd;
import com.gluonhq.attach.ads.InterstitialAdLoadCallback;
import com.gluonhq.attach.ads.OnInitializationCompleteListener;
import com.gluonhq.attach.ads.OnUserEarnedRewardListener;
import com.gluonhq.attach.ads.RequestConfiguration;
import com.gluonhq.attach.ads.RewardedAd;
import com.gluonhq.attach.ads.RewardedAdLoadCallback;

public abstract class DefaultAdsService implements AdsService, BannerAd.Service, InterstitialAd.Service, RewardedAd.Service  {

    private static DefaultAdsService instance;

    private final AdRegistry registry;

    private OnInitializationCompleteListener listener;

    public DefaultAdsService() {
        instance = this;

        registry = new AdRegistry();
        listener = null;
    }

    @Override
    public void initialize(OnInitializationCompleteListener listener) {
        this.listener = listener;
        nativeInitialize();
    }

    @Override
    public void dispose(Ad<?> ad) {
        nativeRemoveAd(ad.getId());
        registry.removeAd(ad.getId());
    }

    @Override
    public BannerAd newBannerAd() {
        BannerAd ad = new BannerAd(registry.nextId(), this);

        registry.addAd(ad);
        nativeBannerAdNew(ad.getId());

        return ad;
    }

    @Override
    public void loadInterstitialAd(String adUnitId, AdRequest adRequest, InterstitialAdLoadCallback callback) {
        InterstitialAd ad = new InterstitialAd(registry.nextId(), this);

        registry.addAd(ad);
        registry.setCallback(ad.getId(), InterstitialAdLoadCallback.class, callback);

        nativeInterstitialAdLoad(ad.getId(), adUnitId);
    }

    @Override
    public void loadRewardedAd(String adUnitId, AdRequest adRequest, RewardedAdLoadCallback callback) {
        RewardedAd ad = new RewardedAd(registry.nextId(), this);

        registry.addAd(ad);
        registry.setCallback(ad.getId(), RewardedAdLoadCallback.class, callback);

        nativeRewardedAdLoad(ad.getId(), adUnitId);
    }

    @Override
    public void setRequestConfiguration(RequestConfiguration requestConfiguration) {
        nativeSetRequestConfiguration(
                requestConfiguration.getTagForChildDirectedTreatment(),
                requestConfiguration.getTagForUnderAgeOfConsent(),
                requestConfiguration.getMaxAdContentRating(),
                requestConfiguration.getTestDeviceIds().toArray(String[]::new));
    }

    @Override
    public void load(BannerAd ad, AdRequest adRequest) {
        nativeBannerAdLoad(ad.getId());
    }

    @Override
    public void show(BannerAd ad) {
        nativeBannerAdShow(ad.getId());
    }

    @Override
    public void hide(BannerAd ad) {
        nativeBannerAdHide(ad.getId());
    }

    @Override
    public void setLayout(BannerAd ad, BannerAd.Layout layout) {
        nativeBannerAdSetLayout(ad.getId(), layout.toString());
    }

    @Override
    public void setAdSize(BannerAd ad, BannerAd.Size size) {
        nativeBannerAdSetAdSize(ad.getId(), size.toString());
    }

    @Override
    public void setAdUnitId(BannerAd ad, String adUnitId) {
        nativeBannerAdSetAdUnitId(ad.getId(), adUnitId);
    }

    @Override
    public void setAdListener(BannerAd ad, AdListener listener) {
        registry.setCallback(ad.getId(), AdListener.class, listener);
        nativeBannerAdSetAdListener(ad.getId());
    }

    @Override
    public void show(InterstitialAd ad) {
        nativeInterstitialAdShow(ad.getId());
    }

    @Override
    public void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback) {
        registry.setCallback(ad.getId(), FullScreenContentCallback.class, callback);
    }

    @Override
    public void show(RewardedAd ad, OnUserEarnedRewardListener listener) {
        registry.setCallback(ad.getId(), OnUserEarnedRewardListener.class, listener);
        nativeRewardedAdShow(ad.getId());
    }

    @Override
    public void setFullScreenContentCallback(RewardedAd ad, FullScreenContentCallback callback) {
        registry.setCallback(ad.getId(), FullScreenContentCallback.class, callback);
    }

    private static void invokeCallback(long id, String callback, String method, String[] params) {
        if (id == -1) {
            instance.listener.onInitializationComplete();
        } else {
            instance.registry.invokeCallback(id, callback, method, params);
        }
    }

    protected abstract void nativeInitialize();

    protected abstract void nativeSetRequestConfiguration(int tagForChildDirectedTreatment, int tagForUnderAgeOfConsent, String maxAdContentRating, String[] testDeviceIds);

    protected abstract void nativeRemoveAd(long id);

    protected abstract void nativeBannerAdNew(long id);

    protected abstract void nativeBannerAdLoad(long id);

    protected abstract void nativeBannerAdShow(long id);

    protected abstract void nativeBannerAdHide(long id);

    protected abstract void nativeBannerAdSetLayout(long id, String layout);

    protected abstract void nativeBannerAdSetAdSize(long id, String size);

    protected abstract void nativeBannerAdSetAdUnitId(long id, String adUnitId);

    protected abstract void nativeBannerAdSetAdListener(long id);

    protected abstract void nativeInterstitialAdLoad(long id, String adUnitId);

    protected abstract void nativeInterstitialAdShow(long id);

    protected abstract void nativeRewardedAdLoad(long id, String adUnitId);

    protected abstract void nativeRewardedAdShow(long id);
}

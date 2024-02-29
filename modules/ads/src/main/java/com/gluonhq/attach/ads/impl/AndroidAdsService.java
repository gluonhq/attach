package com.gluonhq.attach.ads.impl;

import com.gluonhq.attach.ads.*;
import com.gluonhq.attach.ads.OnInitializationCompleteListener;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Android ads service.
 */
public class AndroidAdsService implements AdsService, BannerAd.Service, InterstitialAd.Service, RewardedAd.Service {

    static {
        System.loadLibrary("ads");
    }

    /**
     * Logger used for logging.
     */
    private static final Logger LOG = Logger.getLogger(AndroidAdsService.class.getName());

    /**
     * Stores all ads and their callbacks.
     */
    private static final AdRegistry registry = new AdRegistry();

    /**
     * Called when the initialization of the MobileAds SDK
     * is complete.
     */
    private static OnInitializationCompleteListener listener = null;

    /**
     * Initialize the MobileAds SDK.
     *
     * @param listener the listener that is called on completion
     */
    @Override
    public void initialize(OnInitializationCompleteListener listener) {
        AndroidAdsService.listener = listener;
        nativeInitialize();
    }

    /**
     * Creates a new banner ad.
     *
     * @return the new banner ad
     */
    @Override
    public BannerAd newBannerAd() {
        BannerAd ad = new BannerAd(registry.getId(), this);
        registry.addAd(ad);

        nativeBannerAdNew(ad.getId());

        return ad;
    }

    /**
     * Loads an interstitial ad.
     *
     * @param adUnitId the ad unit id of the ad
     * @param adRequest the ad request to use
     * @param callback the callback when loaded
     */
    @Override
    public void loadInterstitialAd(String adUnitId, AdRequest adRequest, InterstitialAdLoadCallback callback) {
        InterstitialAd ad = new InterstitialAd(registry.getId(), this);
        registry.addAd(ad);
        registry.addCallback(ad.getId(), callback);

        nativeInterstitialAdLoad(ad.getId(), adUnitId);
    }

    /**
     * Loads a rewarded ad.
     *
     * @param adUnitId the ad unit id of the ad
     * @param adRequest the ad request to use
     * @param callback the callback when loaded
     */
    @Override
    public void loadRewardedAd(String adUnitId, AdRequest adRequest, RewardedAdLoadCallback callback) {
        RewardedAd ad = new RewardedAd(registry.getId(), this);
        registry.addAd(ad);
        registry.addCallback(ad.getId(), callback);

        nativeRewardedAdLoad(ad.getId(), adUnitId);
    }

    /**
     * Set the request configuration that is used for all ad requests.
     *
     * @param requestConfiguration the configuration to use for all
     *                             ad requests
     */
    @Override
    public void setRequestConfiguration(RequestConfiguration requestConfiguration) {
        nativeSetRequestConfiguration(
                requestConfiguration.getTagForChildDirectedTreatment(),
                requestConfiguration.getTagForUnderAgeOfConsent(),
                requestConfiguration.getMaxAdContentRating(),
                requestConfiguration.getTestDeviceIds().toArray(new String[0]));
    }

    /**
     * BannerAd
     */

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
        registry.addCallback(ad.getId(), listener);
        nativeBannerAdSetAdListener(ad.getId());
    }

    /**
     * InterstitialAd
     */

    @Override
    public void show(InterstitialAd ad) {
        nativeInterstitialAdShow(ad.getId());
    }

    @Override
    public void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback) {
        registry.addCallback(ad.getId(), callback);
    }

    /**
     * RewardedAd
     */

    @Override
    public void show(RewardedAd ad, OnUserEarnedRewardListener listener) {
        registry.addCallback(ad.getId(), listener);
        nativeRewardedAdShow(ad.getId());
    }

    @Override
    public void setFullScreenContentCallback(RewardedAd ad, FullScreenContentCallback callback) {
        registry.addCallback(ad.getId(), callback);
    }

    /**
     * native code
     */

    private static void invokeCallback(long id, String callbackClass, String callbackMethod, String[] params) {
        LOG.log(Level.INFO, "Invoking callback: { id: " + id + ", class: " + callbackClass + ", method: " + callbackMethod + ", params: " + Arrays.toString(params) + " }");

        if (id == -1) {
            listener.onInitializationComplete();
        } else {
            registry.invokeCallback(id, callbackClass, callbackMethod, params);
        }
    }

    private static native void nativeInitialize();
    private static native void nativeSetRequestConfiguration(int tagForChildDirectedTreatment, int tagForUnderAgeOfConsent, String maxAdContentRating, String[] testDeviceIds);
    private static native void nativeRemoveAd(long id);

    private static native void nativeBannerAdNew(long id);
    private static native void nativeBannerAdLoad(long id);
    private static native void nativeBannerAdShow(long id);
    private static native void nativeBannerAdHide(long id);
    private static native void nativeBannerAdSetLayout(long id, String layout);
    private static native void nativeBannerAdSetAdSize(long id, String size);
    private static native void nativeBannerAdSetAdUnitId(long id, String adUnitId);
    private static native void nativeBannerAdSetAdListener(long id);

    private static native void nativeInterstitialAdLoad(long id, String adUnitId);
    private static native void nativeInterstitialAdShow(long id);

    private static native void nativeRewardedAdLoad(long id, String adUnitId);
    private static native void nativeRewardedAdShow(long id);
}

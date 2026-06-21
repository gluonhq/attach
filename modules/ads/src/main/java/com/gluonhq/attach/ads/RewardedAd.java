package com.gluonhq.attach.ads;

/**
 * This class is used to request and display a rewarded ad.
 */
public class RewardedAd extends Ad<RewardedAd.Service> {

    /**
     * The ad unit ID used for testing.
     */
    public static String TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    /**
     * {@inheritDoc}
     */
    public RewardedAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Shows the ad.
     *
     * @param listener the listener
     */
    public void show(OnUserEarnedRewardListener listener) {
        service.show(this, listener);
    }

    /**
     * Registers a callback to be invoked when ads show and dismiss full screen content.
     *
     * @param callback the callback
     */
    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        service.setFullScreenContentCallback(this, callback);
    }

    public interface Service extends Ad.Service {

        void show(RewardedAd ad, OnUserEarnedRewardListener listener);

        void setFullScreenContentCallback(RewardedAd ad, FullScreenContentCallback callback);
    }
}

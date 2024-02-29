package com.gluonhq.attach.ads;

/**
 * This class is used to request and display a rewarded ad.
 */
public class RewardedAd extends Ad<RewardedAd.Service> {

    /**
     * Defines all methods needed for the ads service.
     */
    public interface Service {

        /**
         * Shows the ad after it is loaded.
         *
         * @param ad the ad to show
         * @param listener the listener to use for when the user has earned
         *                 his reward
         */
        void show(RewardedAd ad, OnUserEarnedRewardListener listener);

        /**
         * Sets the callback for the ad when it is opened, clicked, etc.
         *
         * @param ad the ad to set the callback for
         * @param callback the callback used for the callbacks
         */
        void setFullScreenContentCallback(RewardedAd ad, FullScreenContentCallback callback);
    }

    /**
     * Constructs a new ad with the specified id and service.
     *
     * @param id the unique id of this ad
     * @param service the service used for this ad
     */
    public RewardedAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Shows the ad after it is loaded.
     *
     * @param listener the listener to use for when the user has earned
     *                 his reward
     */
    public void show(OnUserEarnedRewardListener listener) {
        service.show(this, listener);
    }

    /**
     * Sets the callback for the ad when it is opened, clicked, etc.
     *
     * @param callback the callback used for the callbacks
     */
    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        service.setFullScreenContentCallback(this, callback);
    }
}

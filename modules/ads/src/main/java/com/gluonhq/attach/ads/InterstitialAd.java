package com.gluonhq.attach.ads;

/**
 * Shows the user a short interstitial ad.
 */
public class InterstitialAd extends Ad<InterstitialAd.Service> {

    /**
     * Defines all methods needed for the ads service.
     */
    public interface Service {

        /**
         * Shows the ad after it is loaded.
         *
         * @param ad the ad to show
         */
        void show(InterstitialAd ad);

        /**
         * Sets the callback for the ad when it is opened, clicked, etc.
         *
         * @param ad the ad to set the callback for
         * @param callback the callback used for the callbacks
         */
        void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback);
    }

    /**
     * Constructs a new ad with the specified id and service.
     *
     * @param id the unique id of this ad
     * @param service the service used for this ad
     */
    public InterstitialAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Shows the ad after it is loaded.
     */
    public void show() {
        service.show(this);
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

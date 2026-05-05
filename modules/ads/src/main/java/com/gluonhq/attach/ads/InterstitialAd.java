package com.gluonhq.attach.ads;

/**
 * A full page ad experience at natural transition points such as a page change, an app launch, or a game level load.
 * Interstitials use a close button that removes the ad from the user's experience.
 */
public class InterstitialAd extends Ad<InterstitialAd.Service> {

    /**
     * The ad unit ID used for testing.
     */
    public static String TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    /**
     * The ad unit ID used for video testing.
     */
    public static String VIDEO_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/8691691433";

    /**
     * {@inheritDoc}
     */
    public InterstitialAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Shows the ad.
     */
    public void show() {
        service.show(this);
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

        void show(InterstitialAd ad);

        void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback);
    }
}

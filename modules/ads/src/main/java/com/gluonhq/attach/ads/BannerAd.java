package com.gluonhq.attach.ads;

/**
 * Displays a banner with advertisement inside it on the screen.
 */
public class BannerAd extends Ad<BannerAd.Service> {

    /**
     * Defines the position of the {@code BannerAd}.
     */
    public enum Layout {
        /**
         * Position at the top of the screen.
         */
        TOP,

        /**
         * Position at the bottom of the screen.
         */
        BOTTOM,
    }

    /**
     * Defines the size of the {@code BannerAd}.
     */
    public enum Size {
        /**
         * Mobile Marketing Association (MMA) banner ad size
         * (320x50 density-independent pixels).
         */
        BANNER,

        /**
         * Interactive Advertising Bureau (IAB) full banner ad
         * size (468x60 density-independent pixels).
         */
        FULL_BANNER,

        /**
         * Large banner ad size (320x100 density-independent pixels).
         */
        LARGE_BANNER,

        /**
         * Interactive Advertising Bureau (IAB) leaderboard ad
         * size (728x90 density-independent pixels).
         */
        LEADERBOARD,

        /**
         * Interactive Advertising Bureau (IAB) medium rectangle
         * ad size (300x250 density-independent pixels).
         */
        MEDIUM_RECTANGLE,

        /**
         * IAB wide skyscraper ad size (160x600 density-independent
         * pixels).
         */
        WIDE_SKYSCRAPER,

        /**
         * A dynamically sized banner that matches its parent's
         * width and expands/contracts its height to match the
         * ad's content after loading completes.
         */
        FLUID,

        /**
         * An invalid AdSize that will cause the ad request to
         * fail immediately.
         */
        INVALID,
    }

    /**
     * Defines all methods needed for the ads service.
     */
    public interface Service {

        /**
         * Load the banner ad.
         *
         * @param ad the ad to load
         * @param adRequest the {@code AdRequest} to use
         */
        void load(BannerAd ad, AdRequest adRequest);

        /**
         * Shows the ad after it is loaded.
         *
         * @param ad the ad to show
         */
        void show(BannerAd ad);

        /**
         * Hides the ad.
         *
         * @param ad the ad to show
         */
        void hide(BannerAd ad);

        /**
         * Set the layout of the ad.
         *
         * @param ad the target ad
         * @param layout the layout of the ad
         */
        void setLayout(BannerAd ad, Layout layout);

        /**
         * Set the ad size of the ad.
         *
         * @param ad the target ad
         * @param size the size of the ad
         */
        void setAdSize(BannerAd ad, Size size);

        /**
         * Set the ad unit id of the ad.
         *
         * @param ad the target ad
         * @param adUnitId the ad unit id of the ad
         */
        void setAdUnitId(BannerAd ad, String adUnitId);

        /**
         * Set the ad listener of the ad.
         *
         * @param ad the target ad
         * @param listener the listener of the ad
         */
        void setAdListener(BannerAd ad, AdListener listener);
    }

    /**
     * Construct a new {@code BannerAd} with the provided id and
     * service.
     *
     * @param id the unique id of the ad
     * @param service the service for the ad
     */
    public BannerAd(long id, Service service) {
        super(id, service);
    }

    /**
     * Load the ad.
     *
     * @param adRequest the ad request
     */
    public void load(AdRequest adRequest) {
        service.load(this, adRequest);
    }

    /**
     * Show the ad.
     */
    public void show() { service.show(this); }

    /**
     * Hide the ad.
     */
    public void hide() { service.hide(this); }

    /**
     * Set the layout.
     *
     * @param layout the layout of the ad
     */
    public void setLayout(Layout layout) { service.setLayout(this, layout); }

    /**
     * Set the size.
     *
     * @param size the size of the ad
     */
    public void setAdSize(Size size) {
        service.setAdSize(this, size);
    }

    /**
     * Set the ad unit id.
     *
     * @param adUnitId the ad unit id
     */
    public void setAdUnitId(String adUnitId) {
        service.setAdUnitId(this, adUnitId);
    }

    /**
     * Set the ad listener.
     *
     * @param listener the listener
     */
    public void setAdListener(AdListener listener) {
        service.setAdListener(this, listener);
    }
}

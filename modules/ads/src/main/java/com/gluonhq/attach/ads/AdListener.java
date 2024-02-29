package com.gluonhq.attach.ads;

/**
 * A listener for receiving notifications during the lifecycle
 * of an ad.
 */
public abstract class AdListener {

    /**
     * Invokes callbacks that are received from the native code.
     */
    static class Handler implements AdRegistry.CallbackHandler {

        /**
         * Match the callback from native code to a method defined
         * in this class.
         *
         * @param ad the ad the callback is intended for
         * @param callback the class the callback method belongs to
         * @param callbackMethod the callback method to call
         * @param params the params for the callback method
         */
        @Override
        public void handle(Ad<?> ad, Object callback, String callbackMethod, String[] params) {
            AdListener c = (AdListener) callback;

            switch (callbackMethod) {
                case "onAdClicked": c.onAdClicked(); break;
                case "onAdClosed": c.onAdClosed(); break;
                case "onAdFailedToLoad": c.onAdFailedToLoad(); break;
                case "onAdImpression": c.onAdImpression(); break;
                case "onAdLoaded": c.onAdLoaded(); break;
                case "onAdOpened": c.onAdOpened(); break;
                case "onAdSwipeGestureClicked": c.onAdSwipeGestureClicked(); break;
            }
        }
    }

    /**
     * Called when a click is recorded for an ad.
     */
    public void onAdClicked() {
    }

    /**
     * Called when the user is about to return to the application
     * after clicking on an ad.
     */
    public void onAdClosed() {
    }

    /**
     * Called when an ad request failed.
     */
    public void onAdFailedToLoad() {
    }

    /**
     * Called when an impression is recorded for an ad.
     */
    public void onAdImpression() {
    }

    /**
     * Called when an ad is received.
     */
    public void onAdLoaded() {
    }

    /**
     * Called when an ad opens an overlay that covers the screen.
     */
    public void onAdOpened() {
    }

    /**
     * Called when a swipe gesture on an ad is recorded as a click.
     */
    public void onAdSwipeGestureClicked() {
    }
}

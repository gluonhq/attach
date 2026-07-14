package com.gluonhq.attach.ads;

/**
 * A listener for receiving notifications during the lifecycle of an ad.
 */
public abstract class AdListener implements Callback {

    /**
     * Called when a click is recorded for an ad.
     */
    public void onAdClicked() {
        // empty
    }

    /**
     * Called when the user is about to return to the application after clicking on an ad.
     */
    public void onAdClosed() {
        // empty
    }

    /**
     * Called when an ad request failed.
     */
    public void onAdFailedToLoad() {
        // empty
    }

    /**
     * Called when an impression is recorded for an ad.
     */
    public void onAdImpression() {
        // empty
    }

    /**
     * Called when an ad is received.
     */
    public void onAdLoaded() {
        // empty
    }

    /**
     * Called when an ad opens an overlay that covers the screen.
     */
    public void onAdOpened() {
        // empty
    }

    /**
     * Called when a swipe gesture on an ad is recorded as a click.
     */
    public void onAdSwipeGestureClicked() {
        // empty
    }

    static class Adapter implements CallbackAdapter {

        @Override
        public void invoke(Ad<?> ad, Callback callback, String method, String[] params) {
            AdListener c = (AdListener) callback;

            switch (method) {
                case "onAdClicked":
                    c.onAdClicked();
                    break;
                case "onAdClosed":
                    c.onAdClosed();
                    break;
                case "onAdFailedToLoad":
                    c.onAdFailedToLoad();
                    break;
                case "onAdImpression":
                    c.onAdImpression();
                    break;
                case "onAdLoaded":
                    c.onAdLoaded();
                    break;
                case "onAdOpened":
                    c.onAdOpened();
                    break;
                case "onAdSwipeGestureClicked":
                    c.onAdSwipeGestureClicked();
                    break;
            }
        }
    }
}

package com.gluonhq.attach.ads;

/**
 * Callback to be invoked when ads show and dismiss full screen
 * content, such as a fullscreen ad experience or an in-app browser.
 */
public abstract class FullScreenContentCallback {

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
            FullScreenContentCallback c = (FullScreenContentCallback) callback;

            switch (callbackMethod) {
                case "onAdClicked": c.onAdClicked(); break;
                case "onAdDismissedFullScreenContent": c.onAdDismissedFullScreenContent(); break;
                case "onAdFailedToShowFullScreenContent": c.onAdFailedToShowFullScreenContent(); break;
                case "onAdImpression": c.onAdImpression(); break;
                case "onAdShowedFullScreenContent": c.onAdShowedFullScreenContent(); break;
            }
        }
    }

    /**
     * Called when a click is recorded for an ad.
     */
    public void onAdClicked() {
    }

    /**
     * Called when the ad dismissed full screen content.
     */
    public void onAdDismissedFullScreenContent() {
    }

    /**
     * Called when the ad failed to show full screen content.
     */
    public void onAdFailedToShowFullScreenContent() {
    }

    /**
     * Called when an impression is recorded for an ad.
     */
    public void onAdImpression() {
    }

    /**
     * Called when the ad showed the full screen content.
     */
    public void onAdShowedFullScreenContent() {
    }
}

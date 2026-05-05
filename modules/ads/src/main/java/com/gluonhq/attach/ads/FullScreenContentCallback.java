package com.gluonhq.attach.ads;

/**
 * Callback to be invoked when ads show and dismiss full screen content, such as a fullscreen ad experience or an in-app
 * browser.
 */
public abstract class FullScreenContentCallback implements Callback {

    /**
     * Called when a click is recorded for an ad.
     */
    public void onAdClicked() {
        // empty
    }

    /**
     * Called when the ad dismissed full screen content.
     */
    public void onAdDismissedFullScreenContent() {
        // empty
    }

    /**
     * Called when the ad failed to show full screen content.
     */
    public void onAdFailedToShowFullScreenContent() {
        // empty
    }

    /**
     * Called when an impression is recorded for an ad.
     */
    public void onAdImpression() {
        // empty
    }

    /**
     * Called when the ad showed the full screen content.
     */
    public void onAdShowedFullScreenContent() {
        // empty
    }

    static class Adapter implements CallbackAdapter {

        @Override
        public void invoke(Ad<?> ad, Callback callback, String method, String[] params) {
            FullScreenContentCallback c = (FullScreenContentCallback) callback;

            switch (method) {
                case "onAdClicked":
                    c.onAdClicked();
                    break;
                case "onAdDismissedFullScreenContent":
                    c.onAdDismissedFullScreenContent();
                    break;
                case "onAdFailedToShowFullScreenContent":
                    c.onAdFailedToShowFullScreenContent();
                    break;
                case "onAdImpression":
                    c.onAdImpression();
                    break;
                case "onAdShowedFullScreenContent":
                    c.onAdShowedFullScreenContent();
                    break;
            }
        }
    }
}

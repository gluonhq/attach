package com.gluonhq.attach.ads;

public abstract class FullScreenContentCallback implements Callback {

    public void onAdClicked() {
        // empty
    }

    public void onAdDismissedFullScreenContent() {
        // empty
    }

    public void onAdFailedToShowFullScreenContent() {
        // empty
    }

    public void onAdImpression() {
        // empty
    }

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

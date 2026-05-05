package com.gluonhq.attach.ads;

public abstract class AdListener implements Callback {

    public void onAdClicked() {
        // empty
    }

    public void onAdClosed() {
        // empty
    }

    public void onAdFailedToLoad() {
        // empty
    }

    public void onAdImpression() {
        // empty
    }

    public void onAdLoaded() {
        // empty
    }

    public void onAdOpened() {
        // empty
    }

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

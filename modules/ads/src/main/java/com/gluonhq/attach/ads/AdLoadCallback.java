package com.gluonhq.attach.ads;

/**
 * Callback to be invoked when an ad finishes loading.
 *
 * @param <T> the type of ad the callback uses
 */
public abstract class AdLoadCallback<T> implements Callback {

    /**
     * Called when an ad fails to load.
     */
    public void onAdFailedToLoad() {
        // empty
    }

    /**
     * Called when an ad successfully loads.
     *
     * @param ad the loaded ad
     */
    public void onAdLoaded(T ad) {
        // empty
    }

    static class Adapter<T> implements CallbackAdapter {

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Ad<?> ad, Callback callback, String method, String[] params) {
            AdLoadCallback<T> c = (AdLoadCallback<T>) callback;

            switch (method) {
                case "onAdFailedToLoad":
                    c.onAdFailedToLoad();
                    break;
                case "onAdLoaded":
                    c.onAdLoaded((T) ad);
                    break;
            }
        }
    }
}

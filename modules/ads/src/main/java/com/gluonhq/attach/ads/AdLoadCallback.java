package com.gluonhq.attach.ads;

/**
 * Callback to be invoked when an ad finishes loading.
 *
 * @param <T> the exact type of the ad that is loaded
 */
public abstract class AdLoadCallback<T> {

    /**
     * Invokes callbacks that are received from the native code.
     *
     * @param <T> the exact type of the ad that is received when
     *            it is successfully loaded
     */
    static class Handler<T> implements AdRegistry.CallbackHandler {

        /**
         * Match the callback from native code to a method defined
         * in this class.
         *
         * @param ad the ad the callback is intended for
         * @param callback the class the callback method belongs to
         * @param callbackMethod the callback method to call
         * @param params the params for the callback method
         */
        @SuppressWarnings("unchecked")
        @Override
        public void handle(Ad<?> ad, Object callback, String callbackMethod, String[] params) {
            AdLoadCallback<T> c = (AdLoadCallback<T>) callback;

            switch (callbackMethod) {
                case "onAdFailedToLoad": c.onAdFailedToLoad(); break;
                case "onAdLoaded": c.onAdLoaded((T) ad); break;
            }
        }
    }

    /**
     * Called when an ad fails to load.
     */
    public void onAdFailedToLoad() {
    }

    /**
     * Called when an ad successfully loads.
     *
     * @param ad the loaded ad
     */
    public void onAdLoaded(T ad) {
    }
}

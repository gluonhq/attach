package com.gluonhq.attach.ads;

public abstract class AdLoadCallback<T> implements Callback {

    public void onAdFailedToLoad() {
        // empty
    }

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

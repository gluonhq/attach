package com.gluonhq.attach.ads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdRegistry {

    private long id;

    private final Map<String, CallbackAdapter> handlers;

    private final Map<Long, Ad<?>> ads;

    private final Map<Long, Map<String, Callback>> callbacks;

    public AdRegistry() {
        handlers = new HashMap<>();
        ads = new HashMap<>();
        callbacks = new HashMap<>();

        handlers.put(AdListener.class.getSimpleName(), new AdListener.Adapter());
        handlers.put(InterstitialAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Adapter<InterstitialAd>());
        handlers.put(RewardedAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Adapter<RewardedAd>());
        handlers.put(OnUserEarnedRewardListener.class.getSimpleName(), new OnUserEarnedRewardListener.Adapter());
        handlers.put(FullScreenContentCallback.class.getSimpleName(), new FullScreenContentCallback.Adapter());
    }

    public void addAd(Ad<?> ad) {
        ads.put(ad.getId(), ad);
        callbacks.put(ad.getId(), new HashMap<>());
    }

    public void removeAd(long id) {
        ads.remove(id);
        callbacks.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Ad<?>> T getAd(long id) {
        return (T) ads.get(id);
    }

    public <T extends Callback> void setCallback(long id, Class<T> callbackClass, T callback) {
        Map<String, Callback> values = callbacks.get(id);
        String name = callbackClass.getSimpleName();

        values.remove(name);

        if (callback != null) {
            values.put(name, callback);
        }
    }

    public void invokeCallback(long id, String callback, String method, String[] params) {
        CallbackAdapter handler = handlers.get(callback);
        Callback value = callbacks.get(id).get(callback);

        if (value != null) {
            handler.invoke(ads.get(id), value, method, params);
        }
    }

    public long nextId() {
        return id++;
    }
}

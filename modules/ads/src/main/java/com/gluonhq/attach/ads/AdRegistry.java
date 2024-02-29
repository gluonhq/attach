package com.gluonhq.attach.ads;

import com.gluonhq.attach.ads.impl.AndroidAdsService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores all ads with their unique ids.
 */
public class AdRegistry {

    /**
     * Logger used for logging.
     */
    private static final Logger LOG = Logger.getLogger(AdRegistry.class.getName());


    /**
     * Handles the conversion from a callback from the native code
     * to a method from the java side.
     */
    public interface CallbackHandler {

        /**
         * Converts a callback from native code to java.
         *
         * @param ad the ad the callback is intended for
         * @param callback the callback class
         * @param callbackMethod the callback method inside the
         *                       callback class
         * @param params the parameters for the method call
         */
        void handle(Ad<?> ad, Object callback, String callbackMethod, String[] params);
    }

    /**
     * The unique id of the next created ad. This is always
     * incremented by one after an ad got created.
     */
    private long id;

    /**
     * Contains the callback handlers for the ads.
     */
    private final Map<String, CallbackHandler> handlers;

    /**
     * Contains all ads accessible by their ids.
     */
    private final Map<Long, Ad<?>> ads;

    /**
     * Contains all registered callbacks for ads.
     */
    private final Map<Long, List<Callback>> callbacks;

    /**
     * Constructs a new instance and initializes the callback
     * handlers.
     */
    public AdRegistry() {
        handlers = new HashMap<>() {{
            put(AdListener.class.getSimpleName(), new AdListener.Handler());
            put(InterstitialAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Handler<InterstitialAd>());
            put(RewardedAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Handler<RewardedAd>());
            put(OnUserEarnedRewardListener.class.getSimpleName(), new OnUserEarnedRewardListener.Handler());
            put(FullScreenContentCallback.class.getSimpleName(), new FullScreenContentCallback.Handler());
        }};

        ads = new HashMap<>();
        callbacks = new HashMap<>();

        LOG.log(Level.INFO, "AdRegistry(): " + this);
    }

    /**
     * Add an ad to the registry and assign it a unique id.
     *
     * @param ad the ad to add
     */
    public void addAd(Ad<?> ad) {
        if (ads.containsKey(ad.getId())) {
            throw new IllegalArgumentException("Id '" + ad.getId() + "' is already inside the ad registry");
        }

        ads.put(ad.getId(), ad);
        LOG.log(Level.INFO, "addAd(): " + this);
    }

    /**
     * Remove the ad with the provided id.
     *
     * @param id the id of the ad to remove
     */
    public void removeAd(long id) {
        ads.remove(id);
        callbacks.remove(id);
        LOG.log(Level.INFO, "removeAd(): " + this);
    }

    /**
     * Get the ad with the specified id.
     *
     * @param id the id of the ad
     * @return the ad with the specified id
     * @param <T> the exact type of the ad
     */
    @SuppressWarnings("unchecked")
    public <T extends Ad<?>> T getAd(long id) {
        return (T) ads.get(id);
    }

    /**
     * Add a callback to the ad with the specified id.
     *
     * @param id the id of the ad
     * @param callbackObject the callback that is added to the ad
     */
    public void addCallback(long id, Object callbackObject) {
        Callback callback = new Callback(callbackObject);
        List<Callback> callbackList;

        if (callbacks.containsKey(id)) {
            callbackList = callbacks.get(id);
        } else {
            callbackList = new ArrayList<>();
            callbacks.put(id, callbackList);
        }

        // remove same callbacks, then add
        callbackList.removeIf(c -> c.getName().equals(callback.getName()));
        callbackList.add(callback);
        LOG.log(Level.INFO, "addCallback(): " + this);
    }

    /**
     * Called from the native side when a callback occurs.
     *
     * @param id the id of the ad the callback is intended for
     * @param callbackClass the name of the callback class
     * @param callbackMethod the name of the callback method
     * @param params the params for the callback method
     */
    public void invokeCallback(long id, String callbackClass, String callbackMethod, String[] params) {
        LOG.log(Level.INFO, "Registry invokeCallback");
        CallbackHandler handler = handlers.get(callbackClass);
        Callback callback = callbacks.get(id).stream()
                .filter(c -> c.getName().equals(callbackClass))
                .findFirst()
                .orElseThrow();

        LOG.log(Level.INFO, "c: " + callback);

        handler.handle(getAd(id), callback.getCallback(), callbackMethod, params);
        LOG.log(Level.INFO, "invokeCallback(): " + this);
    }

    /**
     * Get the next unique id.
     *
     * @return the next unique id
     */
    public long getId() {
        return id++;
    }

    @Override
    public String toString() {
        return "{id=" + id + ", handlers=" + handlers + ", ads=" + ads + ", callbacks:" + callbacks + "}";
    }
}

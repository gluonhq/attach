package com.gluonhq.attach.ads;

/**
 * Used for callbacks from the native code.
 */
public class Callback {

    /**
     * The name of the callback class.
     */
    private final String name;

    /**
     * The actual callback.
     */
    private final Object callback;

    /**
     * Constructs a new instance and throws an exception if the provided
     * callback is not a known callback.
     *
     * @param callback the callback to use
     */
    public Callback(Object callback) {
        String name = null;

        if (callback instanceof AdListener)
            name = AdListener.class.getSimpleName();
        if (callback instanceof InterstitialAdLoadCallback)
            name = InterstitialAdLoadCallback.class.getSimpleName();
        if (callback instanceof RewardedAdLoadCallback)
            name = RewardedAdLoadCallback.class.getSimpleName();
        if (callback instanceof OnUserEarnedRewardListener)
            name = OnUserEarnedRewardListener.class.getSimpleName();
        if (callback instanceof FullScreenContentCallback)
            name = FullScreenContentCallback.class.getSimpleName();

        if (name == null) {
            throw new IllegalArgumentException("Invalid callback specified");
        }

        this.name = name;
        this.callback = callback;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the callback.
     *
     * @return the callback
     */
    public Object getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return "{name=" + name + ", callback=" + callback + "}";
    }
}

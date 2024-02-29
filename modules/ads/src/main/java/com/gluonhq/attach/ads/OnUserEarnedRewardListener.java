package com.gluonhq.attach.ads;

/**
 * Interface definition for a callback to be invoked when the user
 * earned a reward.
 */
public interface OnUserEarnedRewardListener {

    /**
     * Invokes callbacks that are received from the native code.
     */
    class Handler implements AdRegistry.CallbackHandler {

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
            OnUserEarnedRewardListener c = (OnUserEarnedRewardListener) callback;

            if (callbackMethod.equals("onUserEarnedReward")) {
                c.onUserEarnedReward(params[0], Integer.parseInt(params[1]));
            }
        }
    }

    /**
     * Called when the user earned a reward.
     *
     * @param type the type of the reward
     * @param amount the amount of the reward
     */
    void onUserEarnedReward(String type, int amount);
}

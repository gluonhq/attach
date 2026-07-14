package com.gluonhq.attach.ads;

/**
 * Interface definition for a callback to be invoked when the user earned a reward.
 */
public interface OnUserEarnedRewardListener extends Callback {

    /**
     * Called when the user earned a reward. The app is responsible for crediting the user with the reward.
     *
     * @param type the type of the reward
     * @param amount the amount of the reward
     */
    void onUserEarnedReward(String type, int amount);

    class Adapter implements CallbackAdapter {

        @Override
        public void invoke(Ad<?> ad, Callback callback, String method, String[] params) {
            OnUserEarnedRewardListener c = (OnUserEarnedRewardListener) callback;

            if (method.equals("onUserEarnedReward")) {
                c.onUserEarnedReward(params[0], Integer.parseInt(params[1]));
            }
        }
    }
}

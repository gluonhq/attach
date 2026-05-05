package com.gluonhq.attach.ads;

public interface OnUserEarnedRewardListener extends Callback {

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

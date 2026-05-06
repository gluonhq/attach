package com.gluonhq.attach.ads;

public class RewardedAd extends Ad<RewardedAd.Service> {

    public RewardedAd(long id, Service service) {
        super(id, service);
    }

    public void show(OnUserEarnedRewardListener listener) {
        service.show(this, listener);
    }

    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        service.setFullScreenContentCallback(this, callback);
    }

    public interface Service extends Ad.Service {

        void show(RewardedAd ad, OnUserEarnedRewardListener listener);

        void setFullScreenContentCallback(RewardedAd ad, FullScreenContentCallback callback);
    }
}

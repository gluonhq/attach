package com.gluonhq.attach.ads;

public class InterstitialAd extends Ad<InterstitialAd.Service> {

    public InterstitialAd(long id, Service service) {
        super(id, service);
    }

    public void show() {
        service.show(this);
    }

    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        service.setFullScreenContentCallback(this, callback);
    }

    public interface Service extends Ad.Service {

        void show(InterstitialAd ad);

        void setFullScreenContentCallback(InterstitialAd ad, FullScreenContentCallback callback);
    }
}

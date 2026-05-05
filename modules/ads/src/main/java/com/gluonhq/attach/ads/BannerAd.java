package com.gluonhq.attach.ads;

public class BannerAd extends Ad<BannerAd.Service> {

    public BannerAd(long id, Service service) {
        super(id, service);
    }

    public void load(AdRequest adRequest) {
        service.load(this, adRequest);
    }

    public void show() {
        service.show(this);
    }

    public void hide() {
        service.hide(this);
    }

    public void setLayout(Layout layout) {
        service.setLayout(this, layout);
    }

    public void setAdSize(Size size) {
        service.setAdSize(this, size);
    }

    public void setAdUnitId(String adUnitId) {
        service.setAdUnitId(this, adUnitId);
    }

    public void setAdListener(AdListener listener) {
        service.setAdListener(this, listener);
    }

    public enum Layout {

        TOP,
        BOTTOM,
    }

    public enum Size {

        BANNER,
        FULL_BANNER,
        LARGE_BANNER,
        LEADERBOARD,
        MEDIUM_RECTANGLE,
        WIDE_SKYSCRAPER,
        FLUID,
        INVALID,
    }

    public interface Service extends Ad.Service {

        void load(BannerAd ad, AdRequest adRequest);

        void show(BannerAd ad);

        void hide(BannerAd ad);

        void setLayout(BannerAd ad, Layout layout);

        void setAdSize(BannerAd ad, Size size);

        void setAdUnitId(BannerAd ad, String adUnitId);

        void setAdListener(BannerAd ad, AdListener listener);
    }
}

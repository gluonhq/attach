package com.gluonhq.attach.ads;

public abstract class Ad<T extends Ad.Service> {

    protected final long id;

    protected final T service;

    public Ad(long id, T service) {
        this.id = id;
        this.service = service;
    }

    public void dispose() {
        service.dispose(this);
    }

    public long getId() {
        return id;
    }

    public interface Service {

        void dispose(Ad<?> ad);
    }
}

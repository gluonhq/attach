package com.gluonhq.attach.ads;

/**
 * The base class for all ad types.
 *
 * @param <T> the type of service the ad uses
 */
public abstract class Ad<T extends Ad.Service> {

    /**
     * The unique id of the ad.
     */
    protected final long id;

    /**
     * The service the ad uses.
     */
    protected final T service;

    /**
     * Constructs an ad.
     *
     * @param id the unique id of the ad
     * @param service the service the ad uses
     */
    public Ad(long id, T service) {
        this.id = id;
        this.service = service;
    }

    /**
     * Disposes the ad.
     */
    public void dispose() {
        service.dispose(this);
    }

    /**
     * Returns the unique id of the ad.
     *
     * @return the unique id of the ad
     */
    public long getId() {
        return id;
    }

    public interface Service {

        void dispose(Ad<?> ad);
    }
}

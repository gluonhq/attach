package com.gluonhq.attach.ads;

/**
 * Base class for all ads.
 *
 * @param <T> the service type the ad expects
 */
public abstract class Ad<T> {

    /**
     * The unique id of this ad.
     */
    protected final long id;

    /**
     * The service that provides the methods for this ad.
     */
    protected final T service;

    /**
     * Constructs a new ad with the given id and service.
     *
     * @param id the unique id of this ad
     * @param service the service of this ad
     */
    public Ad(long id, T service) {
        this.id = id;
        this.service = service;
    }

    /**
     * Get the unique id of this ad.
     *
     * @return the unique id
     */
    public long getId() {
        return id;
    }
}

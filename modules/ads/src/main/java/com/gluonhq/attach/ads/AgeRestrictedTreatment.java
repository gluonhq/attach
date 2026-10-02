package com.gluonhq.attach.ads;

/**
 * Age restricted treatment enum options passed to setAgeRestrictedTreatment.
 */
public enum AgeRestrictedTreatment {

    /**
     * Indicates that ad requests should receive CHILD age treatment.
     */
    CHILD,

    /**
     * Indicates that ad requests should receive TEEN age treatment.
     */
    TEEN,

    /**
     * Indicates that no specific age restricted treatment signal applies
     * to the ad request.
     */
    UNSPECIFIED
}

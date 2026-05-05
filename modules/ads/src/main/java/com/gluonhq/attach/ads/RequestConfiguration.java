package com.gluonhq.attach.ads;

import java.util.ArrayList;
import java.util.List;

/**
 * Global configuration that will be used for every AdRequest.
 */
public class RequestConfiguration {

    /**
     * Provides no indication whether ad requests should be treated as child-directed for purposes of the Children’s
     * Online Privacy Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED = -1;

    /**
     * Indicates that ad requests should not be treated as child-directed for purposes of the Children’s Online Privacy
     * Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE = 0;

    /**
     * Indicates that ad requests should be treated as child-directed for purposes of the Children’s Online Privacy
     * Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE = 1;

    /**
     * Indicates that the publisher has not specified whether the ad request should receive treatment for users in the
     * European Economic Area (EEA) under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED = -1;

    /**
     * Indicates the publisher specified that the ad request should not receive treatment for users in the European
     * Economic Area (EEA) under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE = 0;

    /**
     * Indicates the publisher specified that the ad request should receive treatment for users in the European Economic
     * Area (EEA) under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE = 1;

    /**
     * No specified content rating.
     */
    public static final String MAX_AD_CONTENT_RATING_UNSPECIFIED = "";

    /**
     * Content suitable for general audiences, including families.
     */
    public static final String MAX_AD_CONTENT_RATING_G = "G";

    /**
     * Content suitable for most audiences with parental guidance.
     */
    public static final String MAX_AD_CONTENT_RATING_PG = "PG";

    /**
     * Content suitable for teen and older audiences.
     */
    public static final String MAX_AD_CONTENT_RATING_T = "T";

    /**
     * Content suitable only for mature audiences.
     */
    public static final String MAX_AD_CONTENT_RATING_MA = "MA";

    /**
     * The tag for child directed treatment.
     */
    private int tagForChildDirectedTreatment;

    /**
     * The tag for underage of consent.
     */
    private int tagForUnderAgeOfConsent;

    /**
     * The max ad content rating.
     */
    private String maxAdContentRating;

    /**
     * The test device ID's.
     */
    private List<String> testDeviceIds;

    /**
     * Constructs a RequestConfiguration.
     */
    private RequestConfiguration() {
        tagForChildDirectedTreatment = TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
        tagForUnderAgeOfConsent = TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED;
        maxAdContentRating = MAX_AD_CONTENT_RATING_UNSPECIFIED;
        testDeviceIds = new ArrayList<>();
    }

    /**
     * Returns the tag for child directed treatment.
     *
     * @return the tag for child directed treatment
     */
    public int getTagForChildDirectedTreatment() {
        return tagForChildDirectedTreatment;
    }

    /**
     * Returns the tag for underage of consent.
     *
     * @return the tag for underage of consent
     */
    public int getTagForUnderAgeOfConsent() {
        return tagForUnderAgeOfConsent;
    }

    /**
     * Returns the max ad content rating.
     *
     * @return the max ad content rating
     */
    public String getMaxAdContentRating() {
        return maxAdContentRating;
    }

    /**
     * Returns the test device ID's.
     *
     * @return the test device ID's
     */
    public List<String> getTestDeviceIds() {
        return testDeviceIds;
    }

    /**
     * Builder for RequestConfiguration.
     */
    public static class Builder {

        /**
         * The request configuration.
         */
        private final RequestConfiguration config;

        /**
         * Constructs a Builder.
         */
        public Builder() {
            config = new RequestConfiguration();
        }

        /**
         * Builds the RequestConfiguration.
         *
         * @return the request configuration
         */
        public RequestConfiguration build() {
            return config;
        }

        /**
         * This method allows you to specify whether you would like your app to be treated as child-directed for
         * purposes of the Children’s Online Privacy Protection Act (COPPA) -
         * <a href="http://business.ftc.gov/privacy-and-security/childrens-privacy">See here</a>.
         *
         * @param tag the tag
         * @return the builder
         */
        public Builder setTagForChildDirectedTreatment(int tag) {
            config.tagForChildDirectedTreatment = tag;
            return this;
        }

        /**
         * This method allows you to mark your app to receive treatment for users in the European Economic Area (EEA)
         * under the age of consent.
         *
         * @param tag the tag
         * @return the builder
         */
        public Builder setTagForUnderAgeOfConsent(int tag) {
            config.tagForUnderAgeOfConsent = tag;
            return this;
        }

        /**
         * Sets a maximum ad content rating.
         *
         * @param rating the rating
         * @return the builder
         */
        public Builder setMaxAdContentRating(String rating) {
            config.maxAdContentRating = rating;
            return this;
        }

        /**
         * Sets a list of test device IDs corresponding to test devices which will always request test ads.
         *
         * @param ids the ids
         * @return the builder
         */
        public Builder setTestDeviceIds(List<String> ids) {
            config.testDeviceIds = ids;
            return this;
        }
    }
}

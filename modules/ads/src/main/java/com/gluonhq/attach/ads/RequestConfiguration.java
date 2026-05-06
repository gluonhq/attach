package com.gluonhq.attach.ads;

import java.util.ArrayList;
import java.util.List;

public class RequestConfiguration {

    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED = -1;

    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE = 0;

    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE = 1;

    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE = 1;

    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE = 0;

    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED = -1;

    public static final String MAX_AD_CONTENT_RATING_UNSPECIFIED = "";

    public static final String MAX_AD_CONTENT_RATING_G = "G";

    public static final String MAX_AD_CONTENT_RATING_PG = "PG";

    public static final String MAX_AD_CONTENT_RATING_T = "T";

    public static final String MAX_AD_CONTENT_RATING_MA = "MA";

    private int tagForChildDirectedTreatment;

    private int tagForUnderAgeOfConsent;

    private String maxAdContentRating;

    private List<String> testDeviceIds;

    private RequestConfiguration() {
        tagForChildDirectedTreatment = TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
        tagForUnderAgeOfConsent = TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED;
        maxAdContentRating = MAX_AD_CONTENT_RATING_UNSPECIFIED;
        testDeviceIds = new ArrayList<>();
    }

    public int getTagForChildDirectedTreatment() {
        return tagForChildDirectedTreatment;
    }

    public int getTagForUnderAgeOfConsent() {
        return tagForUnderAgeOfConsent;
    }

    public String getMaxAdContentRating() {
        return maxAdContentRating;
    }

    public List<String> getTestDeviceIds() {
        return testDeviceIds;
    }

    public static class Builder {

        private final RequestConfiguration config;

        public Builder() {
            config = new RequestConfiguration();
        }

        public RequestConfiguration build() {
            return config;
        }

        public Builder setTagForChildDirectedTreatment(int tag) {
            config.tagForChildDirectedTreatment = tag;
            return this;
        }

        public Builder setTagForUnderAgeOfConsent(int tag) {
            config.tagForUnderAgeOfConsent = tag;
            return this;
        }

        public Builder setMaxAdContentRating(String rating) {
            config.maxAdContentRating = rating;
            return this;
        }

        public Builder setTestDeviceIds(List<String> ids) {
            config.testDeviceIds = ids;
            return this;
        }
    }
}

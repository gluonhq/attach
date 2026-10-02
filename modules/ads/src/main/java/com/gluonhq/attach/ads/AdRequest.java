package com.gluonhq.attach.ads;

/**
 * An AdRequest contains targeting information used to fetch an ad. Ad requests are created using AdRequest.Builder.
 */
public class AdRequest {

    /**
     * Constructs an AdRequest.
     */
    private AdRequest() {
        // empty
    }

    /**
     * Builds an AdRequest.
     */
    public static class Builder {

        /**
         * The AdRequest for this builder.
         */
        private final AdRequest request;

        /**
         * Constructs a Builder.
         */
        public Builder() {
            request = new AdRequest();
        }

        /**
         * Constructs an AdRequest with the specified attributes.
         *
         * @return the constructed ad request
         */
        public AdRequest build() {
            return request;
        }
    }
}

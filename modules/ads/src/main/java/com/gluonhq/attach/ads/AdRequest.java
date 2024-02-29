package com.gluonhq.attach.ads;

/**
 * Configures the request details for a single ad.
 */
public class AdRequest {

    /**
     * Builds a new {@code AdRequest}.
     */
    public static class Builder {

        /**
         * The final {@code AdRequest}.
         */
        private final AdRequest request;

        /**
         * Constructs a new builder.
         */
        public Builder() {
            request = new AdRequest();
        }

        /**
         * Returns the final {@code AdRequest}.
         *
         * @return the final {@code AdRequest}
         */
        public AdRequest build() {
            return request;
        }
    }

    /**
     * Default constructor.
     */
    private AdRequest() {}
}

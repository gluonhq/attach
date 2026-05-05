package com.gluonhq.attach.ads;

public class AdRequest {

    private AdRequest() {
        // empty
    }

    public static class Builder {

        private final AdRequest request;

        public Builder() {
            request = new AdRequest();
        }

        public AdRequest build() {
            return request;
        }
    }
}

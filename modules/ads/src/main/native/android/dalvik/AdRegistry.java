package com.gluonhq.helloandroid;

import java.util.HashMap;
import java.util.Map;

public class AdRegistry {

    private final Map<Long, Object> ads;

    public AdRegistry() {
        ads = new HashMap<>();
    }

    public void add(long id, Object ad) {
        ads.put(id, ad);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(long id) {
        return (T) ads.get(id);
    }
}

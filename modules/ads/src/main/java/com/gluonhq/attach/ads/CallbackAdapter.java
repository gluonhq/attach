package com.gluonhq.attach.ads;

public interface CallbackAdapter {

    void invoke(Ad<?> ad, Callback callback, String method, String[] params);
}

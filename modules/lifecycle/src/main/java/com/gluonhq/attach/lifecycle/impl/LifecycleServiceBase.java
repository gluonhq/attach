package com.gluonhq.attach.lifecycle.impl;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class LifecycleServiceBase implements LifecycleService {

    private final static Map<LifecycleEvent, List<Runnable>> MAP_EVENTS = new HashMap<>();

    @Override
    public void addListener(LifecycleEvent eventType, Runnable eventHandler) {
        List<Runnable> list = MAP_EVENTS.get(eventType);
        if (list == null) {
            list = new ArrayList<>();
        } else if (list.stream().anyMatch(r -> r == eventHandler)){
            return;
        }
        list.add(eventHandler);
        MAP_EVENTS.put(eventType, list);
    }

    @Override
    public void removeListener(LifecycleEvent eventType, Runnable eventHandler) {
        List<Runnable> list = MAP_EVENTS.get(eventType);
        if (list == null) {
            return;
        }

        list.removeIf(r -> r == eventHandler);
    }

    @Override
    public abstract void shutdown();

    static void doCheck(LifecycleEvent expected) {
        List<Runnable> list = MAP_EVENTS.get(expected);
        if (list == null) {
            return;
        }
        list.forEach(LifecycleServiceBase::run);
    }

    private static void run(Runnable eventHandler) {
        if (eventHandler == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            eventHandler.run();
        } else {
            Platform.runLater(eventHandler);
        }
    }
}

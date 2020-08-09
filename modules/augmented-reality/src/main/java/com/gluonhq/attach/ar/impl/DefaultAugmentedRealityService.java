package com.gluonhq.attach.ar.impl;

import com.gluonhq.attach.ar.AugmentedRealityService;
import com.gluonhq.attach.util.Util;

import java.util.logging.Logger;

public abstract class DefaultAugmentedRealityService implements AugmentedRealityService {

    private static final Logger LOG = Logger.getLogger(DefaultAugmentedRealityService.class.getName());
    protected static boolean debug = Util.DEBUG;
    

    public DefaultAugmentedRealityService() {
    }
}
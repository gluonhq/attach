package com.gluonhq.attach.ar.impl;

import java.util.logging.Logger;

import com.gluonhq.attach.ar.AugmentedRealityService;
import com.gluonhq.attach.util.Constants;

public abstract class DefaultAugmentedRealityService implements AugmentedRealityService {

    private static final Logger LOG = Logger.getLogger(DefaultAugmentedRealityService.class.getName());
    protected static boolean debug;
    

    public DefaultAugmentedRealityService() {
        if ("true".equals(System.getProperty(Constants.ATTACH_DEBUG))) {
            debug = true;
        }
    }
}
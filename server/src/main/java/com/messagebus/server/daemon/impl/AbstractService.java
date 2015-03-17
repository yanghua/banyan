package com.messagebus.server.daemon.impl;

import com.messagebus.server.daemon.IService;

import java.util.Map;

abstract class AbstractService implements IService, Runnable {

    protected Map<String, Object> context;

    public AbstractService(Map<String, Object> context) {
        this.context = context;
    }

}

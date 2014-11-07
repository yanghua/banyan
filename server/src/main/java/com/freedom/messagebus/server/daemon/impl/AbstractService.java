package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.IService;

import java.util.Map;

abstract class AbstractService implements IService, Runnable {

    protected Map<String, Object> context;

    public AbstractService(Map<String, Object> context) {
        this.context = context;
    }

}

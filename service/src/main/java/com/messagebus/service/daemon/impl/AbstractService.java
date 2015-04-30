package com.messagebus.service.daemon.impl;


import com.messagebus.service.daemon.IService;

import java.util.Map;

abstract class AbstractService implements IService, Runnable {

    protected Map<String, Object> context;

    public AbstractService(Map<String, Object> context) {
        this.context = context;
    }

}

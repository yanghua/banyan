package com.messagebus.service.daemon.impl;


import com.messagebus.service.daemon.IService;

import java.util.Map;

abstract class AbstractService implements IService, Runnable {

    protected static final String REVERSE_MESSAGE_ZK_PATH   = "/reverse/message";
    protected static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message/server";

    protected Map<String, Object> context;

    public AbstractService(Map<String, Object> context) {
        this.context = context;
    }

}

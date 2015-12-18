package com.messagebus.service.daemon.impl;


import com.messagebus.service.daemon.IService;

import java.util.Map;

abstract class AbstractService implements IService, Runnable {

    protected static final String REVERSE_MESSAGE_ZK_PATH               = "/reverse/message";
    protected static final String REVERSE_MESSAGE_SOURCE_ZK_PATH        = REVERSE_MESSAGE_ZK_PATH + "/source";
    protected static final String REVERSE_MESSAGE_SINK_ZK_PATH          = REVERSE_MESSAGE_ZK_PATH + "/sink";
    protected static final String REVERSE_MESSAGE_STREAM_ZK_PATH        = REVERSE_MESSAGE_ZK_PATH + "/stream";
    protected static final String REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH = REVERSE_MESSAGE_SOURCE_ZK_PATH + "/secret";
    protected static final String REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH   = REVERSE_MESSAGE_SOURCE_ZK_PATH + "/name";
    protected static final String REVERSE_MESSAGE_SINK_SECRET_ZK_PATH   = REVERSE_MESSAGE_SINK_ZK_PATH + "/secret";
    protected static final String REVERSE_MESSAGE_SINK_NAME_ZK_PATH     = REVERSE_MESSAGE_SINK_ZK_PATH + "/name";
    protected static final String REVERSE_MESSAGE_STREAM_TOKEN_ZK_PATH  = REVERSE_MESSAGE_STREAM_ZK_PATH + "/token";


    protected static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message/server";

    protected Map<String, Object> context;

    public AbstractService(Map<String, Object> context) {
        this.context = context;
    }

}

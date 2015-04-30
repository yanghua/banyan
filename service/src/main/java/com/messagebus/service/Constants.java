package com.messagebus.service;

import com.messagebus.common.AuthInfo;

public class Constants {

    public static final String GLOBAL_CLIENT_POOL      = "clientPool";

    public static final String HOST = "localhost";
    public static final int    PORT = 15672;

    public static final String   HTTP_API_OVERVIEW = "/api/overview";
    public static final String   HTTP_API_QUEUES   = "/api/queues";
    public static final AuthInfo DEFAULT_AUTH_INFO = new AuthInfo("guest", "guest");


}

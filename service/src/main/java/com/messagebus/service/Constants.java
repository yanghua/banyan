package com.messagebus.service;

import com.messagebus.common.AuthInfo;

public class Constants {

    public static final String MQ_HOST_KEY = "mqHost";
    public static final String MQ_PORT_KEY = "mqPort";

    public static final int PORT = 15672;

    public static final String   HTTP_API_OVERVIEW = "/api/overview";
    public static final String   HTTP_API_QUEUES   = "/api/queues";
    public static final AuthInfo DEFAULT_AUTH_INFO = new AuthInfo("guest", "guest");


}

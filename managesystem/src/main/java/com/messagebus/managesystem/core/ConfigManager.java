package com.messagebus.managesystem.core;

import com.messagebus.common.AuthInfo;

public class ConfigManager {

    public static final String HOST = "172.16.206.30";
    public static final int    PORT = 15672;

    public static final String HTTP_API_QUEUES   = "/api/queues";
    public static final String HTTP_API_OVERVIEW = "/api/overview";
    public static final String HTTP_API_NODES    = "/api/nodes";
    public static final String HTTP_API_EXCHANGE = "/api/exchanges";
    public static final String HTTP_API_CHANNEL  = "/api/channels";

    public static final AuthInfo DEFAULT_AUTH_INFO = new AuthInfo("guest", "guest");


}

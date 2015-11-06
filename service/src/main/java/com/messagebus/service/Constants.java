package com.messagebus.service;

import com.messagebus.common.AuthInfo;

public class Constants {

    public static final String ZK_HOST_KEY = "zookeeper.host";
    public static final String ZK_PORT_KEY = "zookeeper.port";

    public static final int PORT = 15672;

    public static final String   HTTP_API_OVERVIEW = "/api/overview";
    public static final String   HTTP_API_QUEUES   = "/api/queues";
    public static final AuthInfo DEFAULT_AUTH_INFO = new AuthInfo("guest", "guest");

    public static final String DEFAULT_CONFIG_RPC_RESPONSE_QUEUE_NAME = "queue.proxy.message.rpc.configRpcResponse";


}

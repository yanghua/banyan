package com.messagebus.server;

import com.messagebus.common.AuthInfo;

public class Constants {

    public static final String KEY_MESSAGEBUS_SERVER_MQ_HOST = "messagebus.server.mq.host";

    public static final String KEY_MESSAGEBUS_SERVER_PUBSUBER_HOST = "messagebus.server.pubsuber.host";
    public static final String KEY_MESSAGEBUS_SERVER_PUBSUBER_PORT = "messagebus.server.pubsuber.port";

    public static final String KEY_MESSAGEBUS_SERVER_DB_SCHEMA   = "messagebus.server.db.schema";
    public static final String KEY_MESSAGEBUS_SERVER_DB_HOST     = "messagebus.server.db.host";
    public static final String KEY_MESSAGEBUS_SERVER_DB_USER     = "messagebus.server.db.user";
    public static final String KEY_MESSAGEBUS_SERVER_DB_PASSWORD = "messagebus.server.db.password";

    public static final String KEY_ARG_CONFIG_FILE_PATH           = "configFilePath";
    public static final String KEY_ARG_SERVER_LOG4J_PROPERTY_PATH = "serverLog4jPropertyPath";
    public static final String KEY_ARG_COMMAND                    = "cmd";

    public static final String KEY_SERVER_CONFIG = "SERVER_CONFIG";

    public static final String GLOBAL_CLIENT_POOL      = "clientPool";
    public static final String GLOBAL_EXCHANGE_MANAGER = "exchangeManager";

    public static final String HOST = "localhost";
    public static final int    PORT = 15672;

    public static final String   HTTP_API_OVERVIEW = "/api/overview";
    public static final String   HTTP_API_QUEUES   = "/api/queues";
    public static final AuthInfo DEFAULT_AUTH_INFO = new AuthInfo("guest", "guest");


}

package com.freedom.messagebus.common;

/**
 * User: yanghua
 * Date: 6/29/14
 * Time: 9:11 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class CONSTS {

    public static final Byte[] EMPTY_BYTE_ARRAY = new Byte[0];

    public static final byte[] EMPTY_PRIMITIVE_BYTE_ARRAY = new byte[0];

    public static final String PUBSUB_ROUTER_CHANNEL = "/router";
    public static final String PUBSUB_CONFIG_CHANNEL = "/config";
    public static final String PUBSUB_EVENT_CHANNEL  = "/event";
    public static final String PUBSUB_AUTH_CHANNEL   = "/auth";

    //second path
    public static final String PUBSUB_AUTH_SEND_PERMISSION_CHANNEL    = "/auth/sendpermission";
    public static final String PUBSUB_AUTH_RECEIVE_PERMISSION_CHANNEL = "/auth/receivepermission";

    public static final String MESSAGEBUS_SERVER_EVENT_STARTED = "started";
    public static final String MESSAGEBUS_SERVER_EVENT_STOPPED = "stopped";


    public static final String PROXY_EXCHANGE_NAME      = "exchange.proxy";
    public static       String DEFAULT_FILE_QUEUE_NAME  = "queue.proxy.log.file";
    public static       String PUBSUB_ROUTING_KEY       = "routingkey.proxy.message.pubsub.#";
    public static       String PUBSUB_QUEUE_NAME_SUFFIX = "-pubsub";

    public static long DEFAULT_DATACENTER_ID_FOR_UUID = 00001L;

}

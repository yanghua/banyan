package com.messagebus.common;

import java.nio.charset.Charset;

/**
 * User: yanghua
 * Date: 6/29/14
 * Time: 9:11 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class Constants {

    public static final Byte[] EMPTY_BYTE_ARRAY = new Byte[0];

    public static final byte[] EMPTY_PRIMITIVE_BYTE_ARRAY = new byte[0];

    public static final String PUBSUB_ROUTER_CHANNEL  = "/router";
    public static final String PUBSUB_CONFIG_CHANNEL  = "/config";
    public static final String PUBSUB_EVENT_CHANNEL   = "/event";
    public static final String PUBSUB_SINK_CHANNEL    = "/sink";
    public static final String PUBSUB_CHANNEL_CHANNEL = "/channel";

    public static final String AUDIT_TYPE_CODE_BEFORE  = "AUDIT_BEFORE";
    public static final String AUDIT_TYPE_CODE_SUCCESS = "AUDIT_SUCCESS";
    public static final String AUDIT_TYPE_CODE_FAILED  = "AUDIT_FAILED";


    public static final String COMMUNICATE_TYPE_PRODUCE           = "produce";
    public static final String COMMUNICATE_TYPE_CONSUME           = "consume";
    public static final String COMMUNICATE_TYPE_PRODUCE_CONSUME   = "produce-consume";
    public static final String COMMUNICATE_TYPE_REQUEST           = "request";
    public static final String COMMUNICATE_TYPE_RESPONSE          = "response";
    public static final String COMMUNICATE_TYPE_REQUEST_RESPONSE  = "request-response";
    public static final String COMMUNICATE_TYPE_PUBLISH           = "publish";
    public static final String COMMUNICATE_TYPE_SUBSCRIBE         = "subscribe";
    public static final String COMMUNICATE_TYPE_PUBLISH_SUBSCRIBE = "publish-subscribe";
    public static final String COMMUNICATE_TYPE_BROADCAST         = "broadcast";

    public static final String MESSAGEBUS_SERVER_EVENT_STARTED = "started";
    public static final String MESSAGEBUS_SERVER_EVENT_STOPPED = "stopped";


    public static final String PROXY_EXCHANGE_NAME        = "exchange.proxy";
    public static final String DEFAULT_FILE_QUEUE_NAME    = "queue.proxy.log.file";
    public static final String NOTIFICATION_EXCHANGE_NAME = "exchange.proxy.message.notification";
    public static final String NOTIFICATION_ROUTING_KEY   = "routingkey.proxy.message.notification.#";

    public static final Charset CHARSET_OF_UTF8 = Charset.forName("UTF-8");

    public static final long DEFAULT_DATACENTER_ID_FOR_UUID = 00001L;

}

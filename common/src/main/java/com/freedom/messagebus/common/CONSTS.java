package com.freedom.messagebus.common;

/**
 * User: yanghua
 * Date: 6/29/14
 * Time: 9:11 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class CONSTS {

    @Deprecated
    public static final String DEFAULT_QUEUE_NAME = "queue";

    @Deprecated
    public static final String ROUTER_NAME_PATTERN = "%s_ROUTER_%s";

    @Deprecated
    public static final String TRANSFER_CENTER_NAME_PATTERN = "%s_TRANSFER_CENTER_%s";

    public static final String PROXY_EXCHANGE_NAME = "exchange.proxy";

    public static final Byte[] EMPTY_BYTE_ARRAY = new Byte[0];

    public static final byte[] EMPTY_PRIMITIVE_BYTE_ARRAY = new byte[0];

    public static final String META_EXCHANGE_NAME = "exchange.meta";

    public static int DEFAULT_AUTH_REQUEST_TIMEOUT_SECONDS = 30;
}

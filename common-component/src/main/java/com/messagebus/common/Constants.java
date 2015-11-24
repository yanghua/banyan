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

    public static final String PROXY_EXCHANGE_NAME     = "exchange.proxy";
    public static final String DEFAULT_FILE_QUEUE_NAME = "queue.proxy.log.file";
    public static final String DEFAULT_CONFIG_RPC_RESPONSE_ROUTING_KEY = "routingkey.proxy.message.rpc.configRpcResponse";

    public static final Charset CHARSET_OF_UTF8 = Charset.forName("UTF-8");

    public static final long DEFAULT_DATACENTER_ID_FOR_UUID = 00001L;

    public static final String MESSAGE_HEADER_KEY_COMPRESS_ALGORITHM = "compressor";

}

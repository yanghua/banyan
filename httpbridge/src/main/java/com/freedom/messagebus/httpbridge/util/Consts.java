package com.freedom.messagebus.httpbridge.util;

public class Consts {

    public static final int HTTP_BASE_CODE = 10000;

    public static final int HTTP_SUCCESS_CODE = HTTP_BASE_CODE + 200;

    public static final int HTTP_FAILED_CODE = HTTP_BASE_CODE + 500;

    public static final int HTTP_NOT_FOUND_CODE = HTTP_BASE_CODE + 404;

    public static final String HTTP_BRIDGE_APP_KEY = "AJDSLFKJALSDFJLAS;DFK";

    public static final String MESSAGE_BUS_KEY = "messagebus";

    public static final int MAX_CONSUME_NUM = 100;

    public static final int MIN_CONSUME_NUM = 1;

    public static final long MAX_CONSUME_TIMEOUT = 60_000L;

    public static final long MIN_CONSUME_TIMEOUT = 100L;

}

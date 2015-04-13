package com.messagebus.httpbridge.util;

public class Constants {

    //region response status code
    private static final int HTTP_BASE_CODE          = 10000;
    public static final  int HTTP_SUCCESS_CODE       = HTTP_BASE_CODE + 200;
    public static final  int HTTP_FAILED_CODE        = HTTP_BASE_CODE + 500;
    public static final  int HTTP_INVALID_PARAM_CODE = HTTP_BASE_CODE + 501;
    public static final  int HTTP_TIMEOUT_CODE       = HTTP_BASE_CODE + 502;
    public static final  int HTTP_NOT_FOUND_CODE     = HTTP_BASE_CODE + 404;
    //endregion

    public static final String KEY_OF_MESSAGEBUS_POOL_OBJ = "messagebusPool";

    //consume mode pull
    public static final int MAX_CONSUME_NUM = 500;
    public static final int MIN_CONSUME_NUM = 1;

    //consume mode push
    public static final long MAX_CONSUME_CONTINUATION_TIMEOUT = 30000L;

    //request timeout
    public static final long MAX_REQUEST_TIMEOUT = 60000L;
    public static final long MIN_REQUEST_TIMEOUT = 100L;

    public static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain";


}

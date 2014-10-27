package com.freedom.messagebus.benchmark.client;

public class TestConfigConstant {

    public static final String APP_KEY    = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    public static final String HOST       = "172.16.206.30";
    public static final int    PORT       = 2181;
    public static final String QUEUE_NAME = "crm";

    public static final String OUTPUT_FILE_PATH_FORMAT = "/tmp/%s.data";

    public static final double MSG_BODY_SIZE_OF_KB = 5;                  //KB
    public static final int    HOLD_TIME_OF_MILLIS = 60_000;             //one minute
    public static final int    FETCH_NUM           = 6;

}

package com.messagebus.benchmark.client;

public class TestConfigConstant {

    public static final String ORIGINAL_PRODUCE_ROUTING_KEY           = "routingkey.proxy.message.procon.erpDemoConsume";
    public static final String DEFAULT_EXCHANGE_NAME_WITHOUT_TOPOLOGY = "amq.fanout";

    public static final String PRODUCER_SECRET     = "kljasdoifqoikjhhhqwhebasdfasdf";
    public static final String PRODUCER_TOKEN      = "hlkasjdhfkqlwhlfalksjdhgssssas";
    public static final String CONSUMER_QUEUE_NAME = "emapDemoConsume";
    public static final String CONSUMER_SECRET     = "zxdjnflakwenklasjdflkqpiasdfnj";

    public static final String OUTPUT_FILE_PATH_FORMAT = "/tmp/%s.data";

    public static final int MSG_BODY_SIZE_OF_BYTE = 3000;                  //B
    public static final int HOLD_TIME_OF_MILLIS   = 60000;             //one minute
    public static final int FETCH_NUM             = 6;

}

package com.freedom.messagebus.common.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MessageType {


    AppMessage("appMessage", 0),

    AuthreqMessage("authreqMessage", 1),

    AuthrespMessage("authrespMessage", 2),

    @Deprecated
    LookupreqMessage("lookupreqMessage", 3),

    @Deprecated
    LookuprespMessage("lookuprespMessage", 4),

    @Deprecated
    CacheExpiredMessage("cacheExpiredMessage", 5);

    private static final Log                      logger     = LogFactory.getLog(MessageType.class);
    private static       Map<String, MessageType> lookupList = new ConcurrentHashMap<>(6);

    static {
        lookupList.put("appMessage", AppMessage);
        lookupList.put("authreqMessage", AuthreqMessage);
        lookupList.put("authrespMessage", AuthrespMessage);
        lookupList.put("lookupreqMessage", LookupreqMessage);
        lookupList.put("lookuprespMessage", LookuprespMessage);
        lookupList.put("cacheExpiredMessage", CacheExpiredMessage);
    }

    public static MessageType lookup(String msgTypeStr) {
        MessageType msgType = lookupList.get(msgTypeStr);

        if (msgType == null) {
            logger.error("[lookup] unknown message type : " + msgTypeStr);
            throw new UnknownError("unknown message type : " + msgTypeStr);
        }

        return msgType;
    }

    private String type;
    private int    index;

    private MessageType(String type, int index) {
        this.type = type;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

}

package com.messagebus.client.message.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MessageType {


    QueueMessage("queue", 0),
    BroadcastMessage("broadcast", 1);

    private static final Log                      logger     = LogFactory.getLog(MessageType.class);
    private static       Map<String, MessageType> lookupList = new ConcurrentHashMap<String, MessageType>(2);

    static {
        lookupList.put("queue", QueueMessage);
        lookupList.put("broadcast", BroadcastMessage);
    }

    public static MessageType lookup(String msgTypeStr) throws UnknownError {
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

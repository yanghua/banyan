package com.freedom.messagebus.common.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MessageType {


    QueueMessage("queue", 0),
    AuthreqMessage("authreq", 1),
    AuthrespMessage("authresp", 2),
    PubSubMessage("pubsub", 3),
    BroadcastMessage("broadcast", 4);

    private static final Log                      logger     = LogFactory.getLog(MessageType.class);
    private static       Map<String, MessageType> lookupList = new ConcurrentHashMap<>(5);

    static {
        lookupList.put("queue", QueueMessage);
        lookupList.put("authreq", AuthreqMessage);
        lookupList.put("authresp", AuthrespMessage);
        lookupList.put("pubsub", PubSubMessage);
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

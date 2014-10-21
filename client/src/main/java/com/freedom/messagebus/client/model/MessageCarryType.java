package com.freedom.messagebus.client.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * message carry type
 */
public enum MessageCarryType {

    PRODUCE,
    CONSUME,
    REQUEST,
    RESPONSE,
    PUBLISH,
    SUBSCRIBE,
    BROADCAST;

    private static final ConcurrentMap<String, MessageCarryType> lookups =
        new ConcurrentHashMap<String, MessageCarryType>(6);

    static {
        lookups.put("produce", PRODUCE);
        lookups.put("consume", CONSUME);
        lookups.put("request", REQUEST);
        lookups.put("response", RESPONSE);
        lookups.put("publish", PUBLISH);
        lookups.put("subscribe", SUBSCRIBE);
        lookups.put("broadcast", BROADCAST);
    }

    @NotNull
    public static MessageCarryType lookup(@NotNull String strType) {
        MessageCarryType result = lookups.get(strType);

        if (result == null)
            throw new UnknownError("can not find the enum item of MessageCarryType which " +
                                       " maped the key : " + strType);

        return result;
    }

    @NotNull
    public String stringOf() {
        for (Map.Entry<String, MessageCarryType> entry : lookups.entrySet()) {
            if (entry.getValue().equals(this))
                return entry.getKey();
        }

        return "";
    }

}

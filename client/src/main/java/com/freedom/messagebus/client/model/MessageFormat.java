package com.freedom.messagebus.client.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * message format
 */
public enum MessageFormat {

    Byte,
    Stream,
    Object,
    Text;        //pure string text and default Message to json string

    private static final ConcurrentMap<String, MessageFormat> lookups =
        new ConcurrentHashMap<String, MessageFormat>(4);

    static {
        lookups.put("byte", Byte);
        lookups.put("stream", Stream);
        lookups.put("object", Object);
        lookups.put("text", Text);
    }

    @NotNull
    public static MessageFormat lookup(@NotNull String strFormat) {
        MessageFormat result = lookups.get(strFormat);

        if (result == null)
            throw new UnknownError("can not find the enum item of MessageFormat which " +
                                       " maped the key : " + strFormat);

        return result;
    }

    @NotNull
    public String stringValue() {
        for (Map.Entry<String, MessageFormat> entry : lookups.entrySet()) {
            if (entry.getValue().equals(this))
                return entry.getKey();
        }

        return "";
    }

}

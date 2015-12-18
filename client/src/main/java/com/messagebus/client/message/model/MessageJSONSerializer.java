package com.messagebus.client.message.model;

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

public class MessageJSONSerializer {

    private static final Log  logger = LogFactory.getLog(MessageJSONSerializer.class);
    private static final Gson gson   = new GsonBuilder().serializeNulls().create();

    public static String serialize(Message msg) {
        return gson.toJson(msg);
    }

    public static Message deSerialize(String msgStr) {
        JsonParser  parser  = new JsonParser();
        JsonElement element = parser.parse(msgStr);

        Message msg = gson.fromJson(element, Message.class);

        return msg;
    }

    public static Message deSerialize(JsonElement msgElement) {
        JsonObject obj = msgElement.getAsJsonObject();
        Message    msg = gson.fromJson(obj.get("message"), Message.class);

        return msg;
    }

    public static String serializeMessages(Collection<Message> msgs) {
        return gson.toJson(msgs);
    }

    public static Message[] deSerializeMessages(String msgArrStr) {
        int i = 0;

        JsonParser  parser  = new JsonParser();
        JsonElement element = parser.parse(msgArrStr);

        if (!element.isJsonArray()) {
            logger.error("unsupported original data. it should be a string of json object array ");
            throw new UnsupportedOperationException("unsupported original data. " +
                    "it should be a string of json object array ");
        }

        Message[] msgs = new Message[element.getAsJsonArray().size()];

        for (JsonElement e : element.getAsJsonArray()) {
            Message msg = gson.fromJson(e, Message.class);

            msgs[i++] = msg;
        }

        return msgs;
    }

}

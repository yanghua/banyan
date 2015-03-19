package com.messagebus.client.message.model;

import com.google.common.base.Strings;
import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

public class MessageJSONSerializer {

    private static final Log  logger = LogFactory.getLog(MessageJSONSerializer.class);
    private static final Gson gson   = new GsonBuilder().serializeNulls().create();


    public static String serialize(Message msg) {
        checkMessageType(msg.getMessageType());

        return gson.toJson(msg);
    }


    public static Message deSerialize(String msgStr, MessageType type) {
        checkMessageType(type);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(msgStr);

        Message msg = gson.fromJson(element, Message.class);

        return msg;
    }

    public static Message deSerialize(JsonElement msgElement, MessageType type) {
        checkMessageType(type);

        JsonObject obj = msgElement.getAsJsonObject();
        Message msg = gson.fromJson(obj.get("message"), Message.class);

        return msg;
    }


    public static String serializeMessages(Collection<Message> msgs) {
        for (Message msg : msgs) {
            checkMessageType(msg.getMessageType());
        }

        return gson.toJson(msgs);
    }


    public static Message[] deSerializeMessages(String msgArrStr, MessageType type) {
        checkMessageType(type);

        int i = 0;

        JsonParser parser = new JsonParser();
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

    private static void checkMessageType(MessageType type) {
        if (!type.equals(MessageType.QueueMessage)) {
            logger.error("[serialize] unsupport message type : " + type.toString() +
                             ", now just support QueueMessage");
            throw new UnsupportedOperationException("unsupport message type : " + type.toString() +
                                                        ", now just support QueueMessage");
        }
    }

    private static void adaptMsgHeader(Message source, Message target) {
        if (!Strings.isNullOrEmpty(source.getReplyTo())) target.setReplyTo(source.getReplyTo());
        target.setMessageId(source.getMessageId());
        if (source.getHeaders() != null) target.setHeaders(source.getHeaders());
        if (!Strings.isNullOrEmpty(source.getAppId())) target.setAppId(source.getAppId());
        if (!Strings.isNullOrEmpty(source.getContentEncoding())) target.setContentEncoding(source.getContentEncoding());
        if (!Strings.isNullOrEmpty(source.getContentType())) target.setContentType(source.getContentType());
        if (!Strings.isNullOrEmpty(source.getCorrelationId())) target.setCorrelationId(source.getCorrelationId());
        target.setPriority(source.getPriority());
        if (source.getTimestamp() != null) target.setTimestamp(source.getTimestamp());
    }

}

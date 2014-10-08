package com.freedom.messagebus.common.message;

import com.freedom.messagebus.common.message.messageBody.AppMessageBody;
import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MessageJSONSerializer {

    private static final Log  logger = LogFactory.getLog(MessageJSONSerializer.class);
    private static final Gson gson   = new GsonBuilder().serializeNulls().create();

    @NotNull
    public static String serialize(@NotNull Message msg) {
        checkMessageType(msg.getMessageType());

        return gson.toJson(msg);
    }

    @NotNull
    public static Message deSerialize(@NotNull String msgStr, @NotNull MessageType type) {
        checkMessageType(type);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(msgStr);

        JsonObject obj = element.getAsJsonObject();
        JsonElement headerElement = obj.get("messageHeader");
        AppMessageBody body = gson.fromJson(obj.get("messageBody"), AppMessageBody.class);
        MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
        IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

        Message msg = new Message();
        msg.setMessageHeader(header);
        msg.setMessageBody(body);
        msg.setMessageType(msgType);

        return msg;
    }

    public static Message deSerialize(@NotNull JsonElement msgElement, @NotNull MessageType type) {
        checkMessageType(type);

        JsonObject obj = msgElement.getAsJsonObject();
        JsonElement headerElement = obj.get("messageHeader");
        AppMessageBody body = gson.fromJson(obj.get("messageBody"), AppMessageBody.class);
        MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
        IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

        Message msg = new Message();
        msg.setMessageHeader(header);
        msg.setMessageBody(body);
        msg.setMessageType(msgType);

        return msg;
    }

    @NotNull
    public static String serializeMessages(@NotNull Collection<Message> msgs) {
        for (Message msg : msgs) {
            checkMessageType(msg.getMessageType());
        }

        return gson.toJson(msgs);
    }

    @NotNull
    public static Message[] deSerializeMessages(@NotNull String msgArrStr, @NotNull MessageType type) {
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
            JsonObject obj = e.getAsJsonObject();
            JsonElement headerElement = obj.get("messageHeader");
            AppMessageBody body = gson.fromJson(obj.get("messageBody"), AppMessageBody.class);
            MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
            IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

            Message msg = new Message();
            msg.setMessageHeader(header);
            msg.setMessageBody(body);
            msg.setMessageType(msgType);
            msgs[i++] = msg;
        }

        return msgs;
    }

    private static void checkMessageType(@NotNull MessageType type) {
        if (!type.equals(MessageType.AppMessage)) {
            logger.error("[serialize] unsupport message type : " + type.toString() +
                             ", now just support AppMessage");
            throw new UnsupportedOperationException("unsupport message type : " + type.toString() +
                                                        ", now just support AppMessage");
        }
    }

}

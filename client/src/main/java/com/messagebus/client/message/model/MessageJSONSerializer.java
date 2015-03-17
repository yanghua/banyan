package com.messagebus.client.message.model;

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

        JsonObject obj = element.getAsJsonObject();
        JsonElement headerElement = obj.get("messageHeader");
        QueueMessage.QueueMessageBody body = gson.fromJson(obj.get("messageBody"), QueueMessage.QueueMessageBody.class);
        MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
        IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

        Message msg = MessageFactory.createMessage(msgType);
        adaptMsgHeader(header, msg.getMessageHeader());
        msg.setMessageBody(body);

        return msg;
    }

    public static Message deSerialize(JsonElement msgElement, MessageType type) {
        checkMessageType(type);

        JsonObject obj = msgElement.getAsJsonObject();
        JsonElement headerElement = obj.get("messageHeader");
        QueueMessage.QueueMessageBody body = gson.fromJson(obj.get("messageBody"), QueueMessage.QueueMessageBody.class);
        MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
        IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

        Message msg = MessageFactory.createMessage(msgType);
        adaptMsgHeader(header, msg.getMessageHeader());
        msg.setMessageBody(body);

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
            JsonObject obj = e.getAsJsonObject();
            JsonElement headerElement = obj.get("messageHeader");
            QueueMessage.QueueMessageBody body = gson.fromJson(obj.get("messageBody"), QueueMessage.QueueMessageBody.class);
            MessageType msgType = gson.fromJson(obj.get("messageType"), MessageType.class);
            IMessageHeader header = gson.fromJson(headerElement, GenericMessageHeader.class);

            Message msg = MessageFactory.createMessage(msgType);
            adaptMsgHeader(header, msg.getMessageHeader());
            msg.setMessageBody(body);
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

    private static void adaptMsgHeader(IMessageHeader source, IMessageHeader target) {
        target.setReplyTo(source.getReplyTo());
        target.setMessageId(source.getMessageId());
        target.setHeaders(source.getHeaders());
        target.setAppId(source.getAppId());
        target.setContentEncoding(source.getContentEncoding());
        target.setContentType(source.getContentType());
        target.setCorrelationId(source.getCorrelationId());
        target.setPriority(source.getPriority());
        target.setTimestamp(source.getTimestamp());
    }

}

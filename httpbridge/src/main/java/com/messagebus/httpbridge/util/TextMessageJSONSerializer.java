package com.messagebus.httpbridge.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by yanghua on 3/30/15.
 */
public class TextMessageJSONSerializer {

    private static final Log  logger = LogFactory.getLog(TextMessageJSONSerializer.class);
    private static final Gson gson   = new GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(byte[].class,
                             new TextMessageJSONSerializer.ByteArrAdapter())
        .create();

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

    private static class ByteArrAdapter extends TypeAdapter<byte[]> {

        @Override
        public void write(JsonWriter jsonWriter, byte[] bytes) throws IOException {
            if (bytes == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(new String(bytes));
            }
        }

        @Override
        public byte[] read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == null) {
                return null;
            }

            String tmp = jsonReader.nextString();
            if (tmp == null)
                return new byte[0];

            return tmp.getBytes();
        }
    }

}

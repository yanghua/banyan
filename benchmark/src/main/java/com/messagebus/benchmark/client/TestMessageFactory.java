package com.messagebus.benchmark.client;

import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;

public class TestMessageFactory {

    public static Message create(MessageType type, int sizeOfByte) {
        Message msg = MessageFactory.createMessage(type);

        byte[] content = generate(sizeOfByte);

        msg.setContent(content);

        return msg;
    }

    public static Message[] create(MessageType type, int sizeOfByte, int num) {
        Message[] msgs = new Message[num];
        Message msg = create(type, sizeOfByte);
        for (int i = 0; i < num; i++) {
            msgs[i] = msg;
        }

        return msgs;
    }

    private static byte[] generate(int sizeOfByte) {
        byte[] result = new byte[sizeOfByte];
        for (int i = 0; i < sizeOfByte; i++) {
            result[i] = (byte) 1;
        }

        return result;
    }

}

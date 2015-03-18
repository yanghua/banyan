package com.messagebus.benchmark.client;

import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;

public class TestMessageFactory {

    public static IMessage create(MessageType type, double sizeOfKB) {
        IMessage msg = MessageFactory.createMessage(type);

        byte[] content = generate(sizeOfKB);

        Message.MessageBody body = new Message.MessageBody();
        body.setContent(content);
        msg.setMessageBody(body);

        return msg;
    }

    public static IMessage[] create(MessageType type, int sizeOfKB, int num) {
        IMessage[] msgs = new IMessage[num];
        IMessage msg = create(type, sizeOfKB);
        for (int i = 0; i < num; i++) {
            msgs[i] = msg;
        }

        return msgs;
    }

    private static byte[] generate(double sizeOfKB) {
        int sizeOfByte = (int) (sizeOfKB * 1024);
        byte[] result = new byte[sizeOfByte];
        for (int i = 0; i < sizeOfByte; i++) {
            result[i] = (byte) 1;
        }

        return result;
    }

}

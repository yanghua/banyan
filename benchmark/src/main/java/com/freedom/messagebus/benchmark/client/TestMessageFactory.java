package com.freedom.messagebus.benchmark.client;

import com.freedom.messagebus.business.message.model.*;

public class TestMessageFactory {

    public static Message create(MessageType type, double sizeOfKB) {
        Message msg = MessageFactory.createMessage(type);

        byte[] content = generate(sizeOfKB);

        switch (type) {
            case QueueMessage: {
                QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                body.setContent(content);
                msg.setMessageBody(body);
            }
            break;

            case PubSubMessage: {
                PubSubMessage.PubSubMessageBody body = new PubSubMessage.PubSubMessageBody();
                body.setContent(content);
                msg.setMessageBody(body);
            }
            break;

            case BroadcastMessage: {
                BroadcastMessage.BroadcastMessageBody body = new BroadcastMessage.BroadcastMessageBody();
                body.setContent(content);
                msg.setMessageBody(body);
            }
            break;
        }

        return msg;
    }

    public static Message[] create(MessageType type, int sizeOfKB, int num) {
        Message[] msgs = new Message[num];
        Message msg = create(type, sizeOfKB);
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

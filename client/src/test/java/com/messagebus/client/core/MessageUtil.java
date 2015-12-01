package com.messagebus.client.core;

import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;

/**
 * Created by yanghua on 3/27/15.
 */
public class MessageUtil {

    public static Message create(int sizeOfByte) {
        Message msg = MessageFactory.createMessage();

        byte[] content = generate(sizeOfByte);

        msg.setContent(content);

        return msg;
    }

    private static byte[] generate(int sizeOfByte) {
        byte[] result = new byte[sizeOfByte];
        for (int i = 0; i < sizeOfByte; i++) {
            result[i] = (byte) 1;
        }

        return result;
    }
}

package com.messagebus.client.message.transfer;

import com.messagebus.client.message.model.IMessageBody;
import com.messagebus.client.message.model.Message;

public class MsgBodyTransfer {

    public static byte[] box(IMessageBody msgBody) {
        if (msgBody instanceof Message.MessageBody) {
            Message.MessageBody body = (Message.MessageBody) msgBody;
            return body.getContent();
        } else {
            throw new ClassCastException("[box] param : msgBody can not be cast to type QueueMessage.QueueMessageBody ");
        }
    }

    public static IMessageBody unbox(byte[] bodyData) {
        Message.MessageBody body = new Message.MessageBody();
        body.setContent(bodyData);

        return body;
    }
}

package com.messagebus.client.message.transfer.bodytransfer;

import com.messagebus.client.message.model.IMessageBody;
import com.messagebus.client.message.model.QueueMessage;
import com.messagebus.client.message.transfer.IMessageBodyTransfer;

public class QueueMsgBodyTransfer implements IMessageBodyTransfer {

    @Override
    public byte[] box(IMessageBody msgBody) {
        if (msgBody instanceof QueueMessage.QueueMessageBody) {
            QueueMessage.QueueMessageBody body = (QueueMessage.QueueMessageBody) msgBody;
            return body.getContent();
        } else {
            throw new ClassCastException("[box] param : msgBody can not be cast to type QueueMessage.QueueMessageBody ");
        }
    }

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent(bodyData);

        return body;
    }
}

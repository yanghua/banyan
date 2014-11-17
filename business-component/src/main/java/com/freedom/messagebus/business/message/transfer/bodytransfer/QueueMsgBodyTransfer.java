package com.freedom.messagebus.business.message.transfer.bodytransfer;

import com.freedom.messagebus.business.message.model.IMessageBody;
import com.freedom.messagebus.business.message.model.QueueMessage;
import com.freedom.messagebus.business.message.transfer.IMessageBodyTransfer;

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

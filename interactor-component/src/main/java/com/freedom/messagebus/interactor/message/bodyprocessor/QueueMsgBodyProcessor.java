package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.common.message.QueueMessage;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;

public class QueueMsgBodyProcessor implements IMessageBodyProcessor {

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

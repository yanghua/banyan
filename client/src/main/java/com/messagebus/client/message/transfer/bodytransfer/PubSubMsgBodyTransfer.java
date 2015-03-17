package com.messagebus.client.message.transfer.bodytransfer;

import com.messagebus.client.message.model.IMessageBody;
import com.messagebus.client.message.model.PubSubMessage;
import com.messagebus.client.message.transfer.IMessageBodyTransfer;

public class PubSubMsgBodyTransfer implements IMessageBodyTransfer {

    @Override
    public byte[] box(IMessageBody msgBody) {
        if (msgBody instanceof PubSubMessage.PubSubMessageBody) {
            PubSubMessage.PubSubMessageBody body = (PubSubMessage.PubSubMessageBody) msgBody;
            return body.getContent();
        } else {
            throw new ClassCastException("[box] param msgBody can not be cast to type : " +
                                             "PubSubMessage.PubSubMessageBody");
        }
    }

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        PubSubMessage.PubSubMessageBody body = new PubSubMessage.PubSubMessageBody();
        body.setContent(bodyData);

        return body;
    }
}

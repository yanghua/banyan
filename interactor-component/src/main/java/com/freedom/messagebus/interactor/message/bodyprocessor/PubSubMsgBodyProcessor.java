package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.common.message.PubSubMessage;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;

public class PubSubMsgBodyProcessor implements IMessageBodyProcessor {

    @Override
    public byte[] box(IMessageBody msgBody) {
        if (msgBody instanceof PubSubMessage.PubSubMessageBody) {
            PubSubMessage.PubSubMessageBody body = (PubSubMessage.PubSubMessageBody)msgBody;
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

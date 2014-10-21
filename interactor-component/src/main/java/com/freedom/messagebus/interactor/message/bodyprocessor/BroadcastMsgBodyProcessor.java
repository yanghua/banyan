package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.BroadcastMessage;
import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;

public class BroadcastMsgBodyProcessor implements IMessageBodyProcessor {

    @Override
    public byte[] box(IMessageBody msgBody) {
        if (msgBody instanceof BroadcastMessage.BroadcastMessageBody) {
            BroadcastMessage.BroadcastMessageBody body = (BroadcastMessage.BroadcastMessageBody) msgBody;
            return body.getContent();
        } else {
            throw new ClassCastException("[box] param msgBody can not be cast to type : " + "BroadcastMessageBody");
        }
    }

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        BroadcastMessage.BroadcastMessageBody body = new BroadcastMessage.BroadcastMessageBody();
        body.setContent(bodyData);

        return body;
    }
}

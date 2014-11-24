package com.freedom.messagebus.client.message.transfer.bodytransfer;

import com.freedom.messagebus.client.message.model.BroadcastMessage;
import com.freedom.messagebus.client.message.model.IMessageBody;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;

public class BroadcastMsgBodyTransfer implements IMessageBodyTransfer {

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

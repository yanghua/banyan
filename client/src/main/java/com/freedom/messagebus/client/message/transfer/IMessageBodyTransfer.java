package com.freedom.messagebus.client.message.transfer;

import com.freedom.messagebus.client.message.model.IMessageBody;

public interface IMessageBodyTransfer {

    public byte[] box(IMessageBody msgBody);

    public IMessageBody unbox(byte[] bodyData);

}

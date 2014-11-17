package com.freedom.messagebus.business.message.transfer;

import com.freedom.messagebus.business.message.model.IMessageBody;

public interface IMessageBodyTransfer {

    public byte[] box(IMessageBody msgBody);

    public IMessageBody unbox(byte[] bodyData);

}

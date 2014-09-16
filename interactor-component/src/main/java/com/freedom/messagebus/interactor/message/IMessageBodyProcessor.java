package com.freedom.messagebus.interactor.message;

import com.freedom.messagebus.common.message.IMessageBody;

public interface IMessageBodyProcessor {

    public byte[] box(IMessageBody msgBody);

    public IMessageBody unbox(byte[] bodyData);

}

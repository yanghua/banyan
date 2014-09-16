package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.google.gson.Gson;

abstract class GenericMsgBodyProcessor implements IMessageBodyProcessor {

    protected static final Gson gson = new Gson();

    @Override
    public byte[] box(IMessageBody msgBody) {
        return gson.toJson(msgBody).getBytes();
    }

    @Override
    public abstract IMessageBody unbox(byte[] bodyData);
}

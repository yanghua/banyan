package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.common.message.messageBody.AuthreqMessageBody;

public class AuthreqMsgBodyProcessor extends GenericMsgBodyProcessor {

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        String str = new String(bodyData);
        return gson.fromJson(str, AuthreqMessageBody.class);
    }
}

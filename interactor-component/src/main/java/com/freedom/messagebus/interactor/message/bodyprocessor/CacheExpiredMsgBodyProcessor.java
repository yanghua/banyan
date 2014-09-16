package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.common.message.messageBody.CacheExpiredMessageBody;

public class CacheExpiredMsgBodyProcessor extends GenericMsgBodyProcessor {

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        String str = new String(bodyData);
        return gson.fromJson(str, CacheExpiredMessageBody.class);
    }
}

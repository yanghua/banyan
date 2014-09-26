package com.freedom.messagebus.interactor.message.bodyprocessor;

import com.freedom.messagebus.common.message.IMessageBody;
import com.freedom.messagebus.common.message.messageBody.AppMessageBody;
import com.freedom.messagebus.common.message.messageBody.EmptyMessageBody;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppMsgBodyProcessor extends GenericMsgBodyProcessor {

    private static final Log logger = LogFactory.getLog(AppMsgBodyProcessor.class);

    @Override
    public IMessageBody unbox(byte[] bodyData) {
        String str = new String(bodyData);
        AppMessageBody appMessageBody = null;
        try {
            appMessageBody = gson.fromJson(str, AppMessageBody.class);
        } catch (JsonSyntaxException e) {
            logger.info("[unbox] occurs a JsonSyntaxException : " + e.getMessage());
        }

        return appMessageBody == null ? new EmptyMessageBody() : appMessageBody;
    }
}

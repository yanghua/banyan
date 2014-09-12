package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.ParamValidateFailedException;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.jetbrains.annotations.NotNull;

/**
 * parameter validate handler
 */
public class ParamValidator extends AbstractHandler {

    private static final String[] messageTypes = {"system", "business"};

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        if (context.getAppKey().length() == 0)
            throw new ParamValidateFailedException(" the field : appkey of MessageContext can not be empty");

        boolean validatedMsgType = false;
        for (String msgType : messageTypes) {
            if (msgType.equals(context.getMsgType())) {
                validatedMsgType = true;
                break;
            }
        }

        if (!validatedMsgType)
            throw new ParamValidateFailedException(" the field : msgType of MessageContext can not be empty");

        if (context.getCarryType().equals(MessageCarryType.CONSUME)) {
            if (context.getRuleValue().isEmpty())
                throw new ParamValidateFailedException("the field : ruleValue of MessageContext can not be empty");
        }

        chain.handle(context);
    }
}

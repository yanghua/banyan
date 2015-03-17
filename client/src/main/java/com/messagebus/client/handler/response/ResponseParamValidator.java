package com.messagebus.client.handler.response;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResponseParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(ResponseParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (!context.getCarryType().equals(MessageCarryType.RESPONSE)) {
            throw new RuntimeException("message carry type should be response ");
        }

        chain.handle(context);
    }
}

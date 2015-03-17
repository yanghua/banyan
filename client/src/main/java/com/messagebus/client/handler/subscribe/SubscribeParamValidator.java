package com.messagebus.client.handler.subscribe;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubscribeParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(SubscribeParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        chain.handle(context);
    }
}

package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumeParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(ConsumeParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        chain.handle(context);
    }
}

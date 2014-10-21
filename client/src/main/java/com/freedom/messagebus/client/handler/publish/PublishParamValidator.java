package com.freedom.messagebus.client.handler.publish;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import org.jetbrains.annotations.NotNull;

public class PublishParamValidator extends AbstractParamValidator {

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        super.handle(context, chain);

        chain.handle(context);
    }
}

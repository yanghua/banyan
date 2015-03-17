package com.messagebus.client.handler.consume;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;

public class OnceConsumeLimiter extends AbstractHandler {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (!context.isSync()) {
//            int limit = ConfigManager.getInstance().getClientConfigMap().get("")
//            context.getChannel().basicQos();
//            context.getChannel().basicRecover(true);
        }
    }
}

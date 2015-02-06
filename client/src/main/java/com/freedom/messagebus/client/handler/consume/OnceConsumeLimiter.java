package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import org.jetbrains.annotations.NotNull;

public class OnceConsumeLimiter extends AbstractHandler {

    @Override
    public void handle( MessageContext context,  IHandlerChain chain) {
        if (!context.isSync()) {
//            int limit = ConfigManager.getInstance().getClientConfigMap().get("")
//            context.getChannel().basicQos();
//            context.getChannel().basicRecover(true);
        }
    }
}

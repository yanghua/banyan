package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import org.jetbrains.annotations.NotNull;

public class OnceConsumeLimiter extends AbstractHandler {

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        if (!context.isSync()){
//            int limit = ConfigManager.getInstance().getClientConfigMap().get("")
//            context.getChannel().basicQos();
//            context.getChannel().basicRecover(true);
        }
    }
}

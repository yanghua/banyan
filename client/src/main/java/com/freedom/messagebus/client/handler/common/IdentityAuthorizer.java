package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.IdentityAuthorizeFailedException;
import com.freedom.messagebus.client.model.HandlerModel;
import org.jetbrains.annotations.NotNull;

/**
 * identity authorize handler
 */
public class IdentityAuthorizer extends AbstractHandler {

    @Override
    public void init(@NotNull HandlerModel handlerModel) {
        //example
    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        //TODO:
        String appKey = context.getAppKey();
        //do auth check
        context.setAuthorized(true);

        if (!context.isAuthorized()) {
            throw new IdentityAuthorizeFailedException(" app key is : " + context.getAppKey());
        } else {
            chain.handle(context);
        }
    }

    @Override
    public void destroy() {
        //example
    }

}

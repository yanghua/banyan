package com.freedom.messagebus.client.handler;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.model.HandlerModel;
import org.jetbrains.annotations.NotNull;

/**
 * the abstract handler
 */
public abstract class AbstractHandler {

    /**
     * do some init things (optional implementation)
     *
     * @param handlerModel the model of handler element
     */
    public void init(@NotNull HandlerModel handlerModel) {
    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    public abstract void handle(@NotNull MessageContext context,
                                @NotNull IHandlerChain chain);

    /**
     * resource clear after the handler would not be used
     */
    public void destroy() {
    }

}

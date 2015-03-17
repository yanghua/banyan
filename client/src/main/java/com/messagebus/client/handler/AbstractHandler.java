package com.messagebus.client.handler;

import com.messagebus.client.MessageContext;
import com.messagebus.client.model.HandlerModel;

/**
 * the abstract handler
 */
public abstract class AbstractHandler {

    /**
     * do some init things (optional implementation)
     *
     * @param handlerModel the model of handler element
     */
    public void init(HandlerModel handlerModel) {
    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    public abstract void handle(MessageContext context,
                                IHandlerChain chain);

    /**
     * resource clear after the handler would not be used
     */
    public void destroy() {
    }

}

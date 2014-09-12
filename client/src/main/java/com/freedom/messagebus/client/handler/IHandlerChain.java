package com.freedom.messagebus.client.handler;

import com.freedom.messagebus.client.MessageContext;

/**
 * interface of handler chain
 */
public interface IHandlerChain {

    /**
     * the trigger of next AbstractHandler's handle method's invoking
     *
     * @param context the message context
     */
    public abstract void handle(MessageContext context);

}

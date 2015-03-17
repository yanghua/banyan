package com.messagebus.client.handler;

import com.messagebus.client.MessageContext;

/**
 * interface of handler chain
 */
public interface IHandlerChain {

    /**
     * the trigger of next AbstractHandler's handle method's invoking
     *
     * @param context the message context
     */
    public void handle(MessageContext context);

}

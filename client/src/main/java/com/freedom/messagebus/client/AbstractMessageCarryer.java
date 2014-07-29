package com.freedom.messagebus.client;

import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.model.MessageCarryType;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
public abstract class AbstractMessageCarryer {

    private   MessageCarryType carryType;
    protected GenericContext   context;

    protected AbstractMessageCarryer() {

    }

    public AbstractMessageCarryer(MessageCarryType carryType, GenericContext context) {
        this.carryType = carryType;
        this.context = context;
    }

    /**
     * main operation method for producing and consuming msg,
     * in the method body, the message will flow through a handler-chain
     *
     * @param context the message context
     */
    public void carry(MessageContext context) {
        IHandlerChain handlerChain = new MessageCarryHandlerChain(carryType, this.context);
        handlerChain.handle(context);
    }
}

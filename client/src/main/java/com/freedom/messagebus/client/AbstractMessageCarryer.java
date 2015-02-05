package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.CONSTS;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
public abstract class AbstractMessageCarryer {

    private   MessageCarryType carryType;
    protected GenericContext   context;

    protected AbstractMessageCarryer() {

    }

    public AbstractMessageCarryer(MessageCarryType carryType) {
        this.carryType = carryType;
    }

    /**
     * main operation method for producing and consuming msg,
     * in the method body, the message will flow through a handler-chain
     *
     * @param context the message context
     */
    public void carry(MessageContext context) {
        //check server state
        if (ConfigManager.getInstance().getServerState().equals(CONSTS.MESSAGEBUS_SERVER_EVENT_STARTED)) {
            IHandlerChain handlerChain = new MessageCarryHandlerChain(carryType, this.getContext());
            handlerChain.handle(context);
        } else {
            throw new RuntimeException("the server is closed. Message can not be carried now!");
        }
    }

    public GenericContext getContext() {
        return context;
    }

    public void setContext(GenericContext context) {
        this.context = context;
    }
}

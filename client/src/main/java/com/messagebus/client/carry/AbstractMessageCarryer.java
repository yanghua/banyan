package com.messagebus.client.carry;

import com.messagebus.client.GenericContext;
import com.messagebus.client.MessageContext;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
abstract class AbstractMessageCarryer {

    private GenericContext context;

    protected MessageContext initMessageContext() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setConfigManager(this.context.getConfigManager());
        msgCtx.setChannel(this.context.getChannel());
        msgCtx.setHost(this.context.getConnection().getAddress().getHostAddress());
        msgCtx.setCarryEventBus(this.context.getCarryEventBus());

        return msgCtx;
    }

    public GenericContext getContext() {
        return context;
    }

    public void setContext(GenericContext context) {
        this.context = context;
    }

}

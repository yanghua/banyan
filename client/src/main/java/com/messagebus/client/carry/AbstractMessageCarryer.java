package com.messagebus.client.carry;

import com.messagebus.client.GenericContext;
import com.messagebus.client.MessageContext;
import com.messagebus.common.Constants;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
abstract class AbstractMessageCarryer {

    private GenericContext context;

    protected void checkState() {
        //check server state
        if (!context.getConfigManager().getServerState().equals(
            Constants.MESSAGEBUS_SERVER_EVENT_STARTED)) {
            throw new RuntimeException("the server is closed. Message can not be carried now!");
        }
    }

    protected MessageContext initMessageContext() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setPubsuberManager(this.context.getPubsuberManager());
        msgCtx.setConfigManager(this.context.getConfigManager());
        msgCtx.setChannel(this.context.getChannel());
        msgCtx.setHost(this.context.getConfigManager().getConfig("messagebus.client.host"));
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

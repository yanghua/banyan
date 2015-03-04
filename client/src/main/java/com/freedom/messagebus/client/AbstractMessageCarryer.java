package com.freedom.messagebus.client;

import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.Constants;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
public abstract class AbstractMessageCarryer {

    private   GenericContext context;
    protected IHandlerChain  handlerChain;

    protected void checkState() {
        //check server state
        if (!context.getConfigManager().getServerState().equals(
            Constants.MESSAGEBUS_SERVER_EVENT_STARTED)) {
            throw new RuntimeException("the server is closed. Message can not be carried now!");
        }
    }

    protected MessageContext initMessageContext() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setAppId(this.context.getAppId());
        msgCtx.setConfigManager(this.context.getConfigManager());
        msgCtx.setChannel(this.context.getChannel());
        msgCtx.setHost(this.context.getConfigManager()
                                   .getClientConfigMap().get("messagebus.client.host").getValue());

        return msgCtx;
    }

    public GenericContext getContext() {
        return context;
    }

    public void setContext(GenericContext context) {
        this.context = context;
    }

}

package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.Constants;

/**
 * abstract class of message carryer
 * in message bus, we treat producer and consumer as carryer
 */
public abstract class AbstractMessageCarryer {

    private   GenericContext context;
    private   MessageContext msgContext;
    protected IHandlerChain  handlerChain;


    public void checkState() {
        //check server state
        if (!ConfigManager.getInstance().getServerState().equals(
            Constants.MESSAGEBUS_SERVER_EVENT_STARTED)) {
            throw new RuntimeException("the server is closed. Message can not be carried now!");
        }
    }

    public GenericContext getContext() {
        return context;
    }

    public void setContext(GenericContext context) {
        this.context = context;
    }

    public MessageContext getMsgContext() {
        return msgContext;
    }

    public void setMsgContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }
}

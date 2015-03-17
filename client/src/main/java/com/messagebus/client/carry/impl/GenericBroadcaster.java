package com.messagebus.client.carry.impl;

import com.messagebus.client.AbstractMessageCarryer;
import com.messagebus.client.MessageContext;
import com.messagebus.client.carry.IBroadcaster;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    private static final Log logger = LogFactory.getLog(GenericBroadcaster.class);

    public GenericBroadcaster() {
    }

    @Override
    public void broadcast(String secret, Message[] msgs, String token) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setToken(token);
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(ctx.getSecret()));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.BROADCAST, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);
    }

}

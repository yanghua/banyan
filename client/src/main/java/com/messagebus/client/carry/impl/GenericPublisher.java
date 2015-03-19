package com.messagebus.client.carry.impl;

import com.messagebus.client.AbstractMessageCarryer;
import com.messagebus.client.MessageContext;
import com.messagebus.client.carry.IPublisher;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher() {
    }

    @Override
    public void publish(String secret, Message[] msgs, String token) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setToken(token);
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(secret));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.PUBLISH, this.getContext());

        this.handlerChain.handle(ctx);
    }

}

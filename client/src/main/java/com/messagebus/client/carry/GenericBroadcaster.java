package com.messagebus.client.carry;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    private static final Log logger = LogFactory.getLog(GenericBroadcaster.class);

    public GenericBroadcaster() {
    }

    @Override
    public void broadcast(String secret, Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(ctx.getSecret()));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.BROADCAST, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);
    }

}

package com.messagebus.client.carry;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher() {
    }

    @Override
    public void publish(String secret, Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(secret));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.PUBLISH, this.getContext());

        this.handlerChain.handle(ctx);
    }

}

package com.freedom.messagebus.client.carry.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.carry.IBroadcaster;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    private static final Log logger = LogFactory.getLog(GenericBroadcaster.class);

    public GenericBroadcaster() {
    }

    @Override
    public void broadcast(Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setSourceNode(this.getContext().getConfigManager()
                              .getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.BROADCAST, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);
    }

}

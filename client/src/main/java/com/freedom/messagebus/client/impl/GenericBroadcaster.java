package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.IBroadcaster;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;

public class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    public GenericBroadcaster() {
        super(MessageCarryType.BROADCAST);
    }

    @Override
    public void broadcast(Message[] msgs) {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setAppId(this.context.getAppId());
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.context.getAppId()));
        ctx.setMessages(msgs);

        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());

        carry(ctx);
    }

}

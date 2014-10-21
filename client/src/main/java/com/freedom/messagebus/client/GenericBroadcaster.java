package com.freedom.messagebus.client;

import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;

public class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    public GenericBroadcaster(GenericContext context) {
        super(MessageCarryType.BROADCAST, context);
    }

    @Override
    public void broadcast(Message[] msgs) {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setAppKey(this.context.getAppKey());
        ctx.setMessages(msgs);

        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());

        carry(ctx);
    }

}

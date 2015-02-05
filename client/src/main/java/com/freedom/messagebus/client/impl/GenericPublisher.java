package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.IPublisher;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher() {
        super(MessageCarryType.PUBLISH);
    }

    @Override
    public void publish(Message[] msgs) {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setAppId(this.context.getAppId());
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.context.getAppId()));
        ctx.setMessages(msgs);

        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());
        carry(ctx);
    }

}

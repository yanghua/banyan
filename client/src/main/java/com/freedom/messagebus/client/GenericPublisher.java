package com.freedom.messagebus.client;

import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher(GenericContext context) {
        super(MessageCarryType.PUBLISH, context);
    }

    @Override
    public void publish(Message[] msgs) {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setAppKey(this.context.getAppKey());
        ctx.setMessages(msgs);

        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());
        carry(ctx);
    }

}

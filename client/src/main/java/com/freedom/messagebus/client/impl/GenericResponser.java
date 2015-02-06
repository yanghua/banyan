package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.IResponser;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.jetbrains.annotations.NotNull;

public class GenericResponser extends AbstractMessageCarryer implements IResponser {

    public GenericResponser() {
        super(MessageCarryType.RESPONSE);
    }

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    @Override
    public void responseTmpMessage( Message msg,  String queueName) {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.RESPONSE);

        ctx.setAppId(super.context.getAppId());

        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(super.context.getAppId()));
        ctx.setMessages(new Message[]{msg});
        ctx.setTempQueueName(queueName);

        ctx.setPool(super.context.getPool());
        ctx.setConnection(super.context.getConnection());

        carry(ctx);
    }
}

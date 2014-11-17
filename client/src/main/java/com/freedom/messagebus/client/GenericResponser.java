package com.freedom.messagebus.client;

import com.freedom.messagebus.business.message.model.Message;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.jetbrains.annotations.NotNull;

public class GenericResponser extends AbstractMessageCarryer implements IResponser {

    public GenericResponser(GenericContext context) {
        super(MessageCarryType.RESPONSE, context);
    }

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    @Override
    public void responseTmpMessage(@NotNull Message msg, @NotNull String queueName) {
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

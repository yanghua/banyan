package com.freedom.messagebus.client;

import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;
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
        final MessageContext cxt = new MessageContext();
        cxt.setCarryType(MessageCarryType.RESPONSE);
        cxt.setAppKey(super.context.getAppKey());
        cxt.setMessages(new Message[]{msg});
        cxt.setTempQueueName(queueName);

        cxt.setPool(this.context.getPool());
        cxt.setConnection(this.context.getConnection());

        carry(cxt);
    }
}

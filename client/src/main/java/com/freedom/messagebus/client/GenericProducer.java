package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

/**
 * a generic producer implements the IProducer interface
 */
class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);


    public GenericProducer(GenericContext context) {
        super(MessageCarryType.PRODUCE, context);
    }

    /**
     * simple producer just produces a message
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produce(@NotNull Message msg,
                        @NotNull String to) {
        MessageContext context = this.innerProduce(super.context.getAppKey(), to);
        context.setMessages(new Message[]{msg});
        carry(context);
    }

    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produceWithTX(@NotNull Message msg,
                              @NotNull String to) {
        MessageContext context = this.innerProduce(super.context.getAppKey(), to);
        context.setMessages(new Message[]{msg});
        context.setEnableTransaction(true);
        carry(context);
    }

    /**
     * a producer produces a set of messages
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    @Override
    public void batchProduce(@NotNull Message[] msgs,
                             @NotNull String to) {
        MessageContext context = this.innerProduce(super.context.getAppKey(), to);
        context.setMessages(msgs);
        carry(context);
    }

    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    @Override
    public void batchProduceWithTX(@NotNull Message[] msgs,
                                   @NotNull String to) {
        MessageContext context = this.innerProduce(super.context.getAppKey(), to);
        context.setMessages(msgs);
        context.setEnableTransaction(true);
        carry(context);
    }

    private MessageContext innerProduce(@NotNull String appKey,
                                        @NotNull String to) {
        MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setAppKey(appKey);
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(to);
        context.setQueueNode(node);

        context.setPool(this.context.getPool());
        context.setConnection(this.context.getConnection());

        return context;
    }
}

package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.IProducer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

/**
 * a generic producer implements the IProducer interface
 */
public class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);


    public GenericProducer() {
        super(MessageCarryType.PRODUCE);
    }

    /**
     * simple producer just produces a message
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produce( Message msg,
                         String to) {
        MessageContext ctx = this.innerProduce(super.context.getAppId(), to);
        ctx.setMessages(new Message[]{msg});
        carry(ctx);
    }

    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produceWithTX( Message msg,
                               String to) {
        MessageContext context = this.innerProduce(super.context.getAppId(), to);
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
    public void batchProduce( Message[] msgs,
                              String to) {
        MessageContext context = this.innerProduce(super.context.getAppId(), to);
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
    public void batchProduceWithTX( Message[] msgs,
                                    String to) {
        MessageContext context = this.innerProduce(super.context.getAppId(), to);
        context.setMessages(msgs);
        context.setEnableTransaction(true);
        carry(context);
    }

    private MessageContext innerProduce( String appId,
                                         String to) {
        MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setAppId(appId);

        context.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(appId));
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(to);
        context.setTargetNode(node);

        context.setPool(this.context.getPool());
        context.setConnection(this.context.getConnection());

        return context;
    }


}

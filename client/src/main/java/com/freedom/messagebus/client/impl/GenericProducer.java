package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IProducer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * a generic producer implements the IProducer interface
 */
public class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);

    /**
     * simple producer just produces a message
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produce(Message msg,
                        String to) {
        MessageContext ctx = this.innerProduce(this.getContext().getAppId(), to);
        ctx.setMessages(new Message[]{msg});
        commonCarry(ctx);
    }

    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    @Override
    public void produceWithTX(Message msg,
                              String to) {
        MessageContext context = this.innerProduce(this.getContext().getAppId(), to);
        context.setMessages(new Message[]{msg});
        context.setEnableTransaction(true);
        commonCarry(context);
    }

    /**
     * a producer produces a set of messages
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    @Override
    public void batchProduce(Message[] msgs,
                             String to) {
        MessageContext context = this.innerProduce(this.getContext().getAppId(), to);
        context.setMessages(msgs);
        commonCarry(context);
    }

    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    @Override
    public void batchProduceWithTX(Message[] msgs,
                                   String to) {
        MessageContext context = this.innerProduce(this.getContext().getAppId(), to);
        context.setMessages(msgs);
        context.setEnableTransaction(true);
        commonCarry(context);
    }

    private MessageContext innerProduce(String appId,
                                        String to) {
        MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setAppId(appId);

        context.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(appId));
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(to);
        context.setTargetNode(node);

        context.setPool(this.getContext().getPool());
        context.setConnection(this.getContext().getConnection());

        return context;
    }

    private void commonCarry(MessageContext ctx) {
        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.PRODUCE,
                                                         this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        //consume
        this.genericProduce(ctx, handlerChain);
    }

    private void genericProduce(MessageContext context,
                                IHandlerChain chain) {
        try {
            if (context.isEnableTransaction()) {
                for (Message msg : context.getMessages()) {
                    IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                    byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());
                    ProxyProducer.produceWithTX(Constants.PROXY_EXCHANGE_NAME,
                                                context.getChannel(),
                                                context.getTargetNode().getRoutingKey(),
                                                msgBody,
                                                properties);
                }
            } else {
                for (Message msg : context.getMessages()) {
                    IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                    byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          context.getTargetNode().getRoutingKey(),
                                          msgBody,
                                          properties);
                }
            }

            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        }
    }

}

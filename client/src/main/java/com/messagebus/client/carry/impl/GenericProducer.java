package com.messagebus.client.carry.impl;

import com.messagebus.business.model.Node;
import com.messagebus.client.AbstractMessageCarryer;
import com.messagebus.client.MessageContext;
import com.messagebus.client.carry.IProducer;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a generic producer implements the IProducer interface
 */
public class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);

    public GenericProducer() {
    }

    /**
     * simple producer just produces a message
     *
     * @param secret
     * @param to     the message's destination
     * @param msg    a general message
     * @param token
     */
    @Override
    public void produce(String secret, String to, Message msg, String token) {
        MessageContext ctx = this.innerProduce(secret, to, token);
        ctx.setMessages(new Message[]{msg});
        commonCarry(ctx);
    }

    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param secret
     * @param to     the message's destination
     * @param msg    a general message
     * @param token
     */
    @Override
    public void produceWithTX(String secret, String to, Message msg, String token) {
        MessageContext context = this.innerProduce(secret, to, token);
        context.setMessages(new Message[]{msg});
        context.setEnableTransaction(true);
        commonCarry(context);
    }

    /**
     * a producer produces a set of messages
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    @Override
    public void batchProduce(String secret, String to, Message[] msgs, String token) {
        MessageContext context = this.innerProduce(secret, to, token);
        context.setMessages(msgs);
        commonCarry(context);
    }

    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    @Override
    public void batchProduceWithTX(String secret, String to, Message[] msgs, String token) {
        MessageContext context = this.innerProduce(secret, to, token);
        context.setMessages(msgs);
        context.setEnableTransaction(true);
        commonCarry(context);
    }

    private MessageContext innerProduce(String secret,
                                        String to, String token) {
        MessageContext context = initMessageContext();
        context.setSecret(secret);
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setSourceNode(this.getContext().getConfigManager()
                                  .getSecretNodeMap().get(secret));
        Node node = this.getContext().getConfigManager().getProconNodeMap().get(to);
        context.setTargetNode(node);
        context.setToken(token);

        return context;
    }

    private void commonCarry(MessageContext ctx) {
        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.PRODUCE, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);
    }

}

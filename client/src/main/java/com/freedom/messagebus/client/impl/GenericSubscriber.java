package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.handler.common.ReceiveEventLoop;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;

public class GenericSubscriber extends AbstractMessageCarryer implements ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    public ISubscribeManager subscribe(List<String> subQueueNames,
                                       IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.SUBSCRIBE);
        ctx.setAppId(this.getContext().getAppId());
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));

        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());
        ctx.setListener(receiveListener);
        ctx.setSync(false);

        this.preProcessSubQueueNames(subQueueNames);
        ctx.setSubQueueNames(subQueueNames);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.SUBSCRIBE,
                                                         this.getContext());
        //launch pre pipeline
        this.handlerChain.startPre();
        this.handlerChain.handle(ctx);

        //consume
        this.genericSubscribe(ctx, handlerChain);

        //launch post pipeline
        this.handlerChain.startPost();
        handlerChain.handle(ctx);

        return new ISubscribeManager() {
            @Override
            public void close() {
                synchronized (this) {
                    if (ctx.getReceiveEventLoop().isAlive()) {
                        ctx.getReceiveEventLoop().shutdown();
                    }
                }
            }

            @Override
            public void addSubscriber(String subQueueName) {
                synchronized (this) {
                    ctx.getSubQueueNames().add(subQueueName);
                }
            }

            @Override
            public void removeSubscriber(String subQueueName) {
                synchronized (this) {
                    ctx.getSubQueueNames().remove(subQueueName);
                }
            }
        };
    }

    private void preProcessSubQueueNames(List<String> subQueueNames) {
        for (int i = 0; i < subQueueNames.size(); i++) {
            String subQueueName = subQueueNames.get(i);
            if (subQueueName.endsWith(Constants.PUBSUB_QUEUE_NAME_SUFFIX))
                subQueueNames.set(i, subQueueName.replaceAll(Constants.PUBSUB_QUEUE_NAME_SUFFIX, ""));
        }
    }

    private void genericSubscribe(MessageContext context,
                                  IHandlerChain chain) {
        ReceiveEventLoop eventLoop = new ReceiveEventLoop();
        eventLoop.setChain(chain);
        eventLoop.setContext(context);
        eventLoop.setChannelDestroyer(context.getDestroyer());
        eventLoop.setCurrentConsumer((QueueingConsumer) context.getOtherParams().get("consumer"));
        context.setReceiveEventLoop(eventLoop);

        //repeat current handler
        if (chain instanceof MessageCarryHandlerChain) {
            eventLoop.startEventLoop();
        } else {
            throw new RuntimeException("the type of chain's instance is not MessageCarryHandlerChain");
        }
    }

}

package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IBroadcaster;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
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
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    private static final Log logger = LogFactory.getLog(GenericBroadcaster.class);

    private static volatile GenericBroadcaster instance;
    private Channel channel;

    private GenericBroadcaster(AbstractPool<Channel> pool) {
        this.channel = pool.getResource();
    }

    public static GenericBroadcaster defaultBroadcaster(AbstractPool<Channel> pool) {
        synchronized (GenericBroadcaster.class) {
            if (instance == null) {
                synchronized (GenericBroadcaster.class) {
                    instance = new GenericBroadcaster(pool);
                }
            }
        }

        return instance;
    }

    @Override
    public void broadcast(Message[] msgs) {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setAppId(this.getContext().getAppId());
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setMessages(msgs);
        ctx.setChannel(this.channel);

        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.BROADCAST,
                                                         this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);
    }

}

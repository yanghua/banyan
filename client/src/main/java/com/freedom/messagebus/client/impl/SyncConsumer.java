package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * a sync consumer.
 */
public class SyncConsumer extends AbstractMessageCarryer {

    private static final Log logger = LogFactory.getLog(SyncConsumer.class);

    private static volatile SyncConsumer instance;
    private                 Channel      channel;

    private SyncConsumer(AbstractPool<Channel> pool) {
        this.channel = pool.getResource();
    }

    public static SyncConsumer defaultSyncConsumer(AbstractPool<Channel> pool) {
        synchronized (SyncConsumer.class) {
            if (instance == null) {
                synchronized (SyncConsumer.class) {
                    instance = new SyncConsumer(pool);
                }
            }
        }

        return instance;
    }

    public List<Message> consume(int expectedNum) {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setAppId(this.getContext().getAppId());
        ctx.setChannel(this.channel);
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());
        ctx.setConsumeMsgNum(expectedNum);
        ctx.setSync(true);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.CONSUME,
                                                         this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        return ctx.getConsumeMsgs();
    }

}

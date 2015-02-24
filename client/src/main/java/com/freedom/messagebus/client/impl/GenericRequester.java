package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IRequester;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.MessageResponseTimeoutException;
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
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class GenericRequester extends AbstractMessageCarryer implements IRequester {

    private static final Log logger = LogFactory.getLog(GenericRequester.class);

    private static volatile GenericRequester instance;
    private Channel channel;

    private GenericRequester(AbstractPool<Channel> pool) {
        this.channel = pool.getResource();
    }

    public static GenericRequester defaultRequester(AbstractPool<Channel> pool) {
        synchronized (GenericRequester.class) {
            if (instance == null) {
                synchronized (GenericRequester.class) {
                    instance = new GenericRequester(pool);
                }
            }
        }

        return instance;
    }

    /**
     * send a request and got a response
     *
     * @param msg     request message
     * @param to      send to destination
     * @param timeout response wait timeout
     * @return Message the response
     * @throws com.freedom.messagebus.client.MessageResponseTimeoutException
     */
    @Override
    public Message request(Message msg,
                           String to,
                           long timeout) throws MessageResponseTimeoutException {
        MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.REQUEST);
        ctx.setAppId(this.getContext().getAppId());
        ctx.setChannel(this.channel);

        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(to);
        ctx.setTargetNode(node);
        ctx.setTimeout(timeout);
        ctx.setMessages(new Message[]{msg});

        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.REQUEST,
                                                         this.getContext());
        //launch pre pipeline
        this.handlerChain.handle(ctx);

        if (ctx.isTimeout() || ctx.getConsumedMsg() == null)
            throw new MessageResponseTimeoutException("message request time out.");

        return ctx.getConsumedMsg();
    }


}

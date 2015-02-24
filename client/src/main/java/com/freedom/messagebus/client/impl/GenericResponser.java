package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IResponser;
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
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class GenericResponser extends AbstractMessageCarryer implements IResponser {

    private static final Log logger = LogFactory.getLog(GenericResponser.class);

    private static volatile GenericResponser instance;
    private                 Channel          channel;

    private GenericResponser(AbstractPool<Channel> pool) {
        this.channel = pool.getResource();
    }

    public static GenericResponser defaultResponser(AbstractPool<Channel> pool) {
        synchronized (GenericResponser.class) {
            if (instance == null) {
                synchronized (GenericResponser.class) {
                    instance = new GenericResponser(pool);
                }
            }
        }

        return instance;
    }

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    @Override
    public void responseTmpMessage(Message msg, String queueName) {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.RESPONSE);
        ctx.setChannel(this.channel);
        ctx.setAppId(this.getContext().getAppId());

        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setMessages(new Message[]{msg});
        ctx.setTempQueueName(queueName);

        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RESPONSE,
                                                         this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        //consume
        this.genericResponse(ctx, handlerChain);
    }

    private void genericResponse(MessageContext context, IHandlerChain chain) {
        Message responseMsg = context.getMessages()[0];
        IMessageBodyTransfer msgBodyProcessor =
            MessageBodyTransferFactory.createMsgBodyProcessor(responseMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(responseMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(responseMsg.getMessageHeader());
        try {
            ProxyProducer.produce("",
                                  context.getChannel(),
                                  context.getTempQueueName(),
                                  msgBody,
                                  properties);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "genericResponse");
            throw new RuntimeException(e);
        }
    }

}

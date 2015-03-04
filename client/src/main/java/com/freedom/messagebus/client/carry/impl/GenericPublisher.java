package com.freedom.messagebus.client.carry.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.carry.IPublisher;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher() {
    }

    @Override
    public void publish(Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setSourceNode(this.getContext().getConfigManager()
                              .getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setMessages(msgs);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.PUBLISH, this.getContext());

        this.handlerChain.handle(ctx);

        //consume
        this.genericPublish(ctx, handlerChain);
    }

    public void genericPublish(MessageContext context, IHandlerChain chain) {
        try {
            for (Message msg : context.getMessages()) {
                IMessageBodyTransfer msgBodyProcessor =
                    MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());
                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      Constants.PUBSUB_ROUTING_KEY,
                                      msgBody,
                                      properties);
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "genericPublish");
            throw new RuntimeException(e);
        }
    }

}

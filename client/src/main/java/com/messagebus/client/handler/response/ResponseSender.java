package com.messagebus.client.handler.response;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class ResponseSender extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(ResponseSender.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Message responseMsg = context.getMessages()[0];
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(responseMsg);
        try {
            ProxyProducer.produce("",
                                  context.getChannel(),
                                  context.getTempQueueName(),
                                  responseMsg.getContent(),
                                  properties);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "response sender");
            throw new RuntimeException(e);
        }

    }
}

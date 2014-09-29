package com.freedom.messagebus.client.handler.response;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

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
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        Message responseMsg = context.getMessages()[0];
        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(responseMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(responseMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderProcessor.box(responseMsg.getMessageHeader());
        try {
            ProxyProducer.produce("",
                                  context.getChannel(),
                                  context.getTempQueueName(),
                                  msgBody,
                                  properties);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        } finally {
            context.getDestroyer().destroy(context.getChannel());
        }

    }
}

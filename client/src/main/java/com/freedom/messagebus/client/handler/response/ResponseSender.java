package com.freedom.messagebus.client.handler.response;

import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
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
        IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(responseMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(responseMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(responseMsg.getMessageHeader());
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

package com.freedom.messagebus.client.handler.request;

import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RealRequester extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealRequester.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle( MessageContext context,  IHandlerChain chain) {
        Message reqMsg = context.getMessages()[0];
        IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(reqMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(reqMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(reqMsg.getMessageHeader());
        try {
            ProxyProducer.produceWithTX(CONSTS.PROXY_EXCHANGE_NAME,
                                        context.getChannel(),
                                        context.getTargetNode().getRoutingKey(),
                                        msgBody,
                                        properties);
            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        }
    }
}

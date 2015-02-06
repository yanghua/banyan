package com.freedom.messagebus.client.handler.broadcast;

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

public class RealBroadcaster extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealBroadcaster.class);

    @Override
    public void handle( MessageContext context,  IHandlerChain chain) {
        try {
            for (Message msg : context.getMessages()) {
                IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());
                ProxyProducer.produce(CONSTS.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      CONSTS.PUBSUB_ROUTING_KEY,
                                      msgBody,
                                      properties);
            }

            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        } finally {
            context.getDestroyer().destroy(context.getChannel());
        }
    }
}

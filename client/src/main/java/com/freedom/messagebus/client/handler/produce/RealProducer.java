package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * the real producer
 */
public class RealProducer extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealProducer.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context,
                       IHandlerChain chain) {
        try {
            if (context.isEnableTransaction()) {
                for (Message msg : context.getMessages()) {
                    IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                    byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());
                    ProxyProducer.produceWithTX(Constants.PROXY_EXCHANGE_NAME,
                                                context.getChannel(),
                                                context.getTargetNode().getRoutingKey(),
                                                msgBody,
                                                properties);
                }
            } else {
                for (Message msg : context.getMessages()) {
                    IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                    byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          context.getTargetNode().getRoutingKey(),
                                          msgBody,
                                          properties);
                }
            }

            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        } finally {
            context.getDestroyer().destroy(context.getChannel());
        }
    }

}

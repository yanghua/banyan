package com.messagebus.client.handler.produce;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 3/17/15.
 */
public class RealProduce extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealProduce.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        try {
            if (context.isEnableTransaction()) {
                for (Message msg : context.getMessages()) {
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);
                    ProxyProducer.produceWithTX(Constants.PROXY_EXCHANGE_NAME,
                                                context.getChannel(),
                                                context.getTargetNode().getRoutingKey(),
                                                msg.getContent(),
                                                properties);
                }
            } else {
                for (Message msg : context.getMessages()) {
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          context.getTargetNode().getRoutingKey(),
                                          msg.getContent(),
                                          properties);
                }
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "real produce");
            throw new RuntimeException(e);
        }
    }
}

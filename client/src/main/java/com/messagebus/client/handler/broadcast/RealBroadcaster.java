package com.messagebus.client.handler.broadcast;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class RealBroadcaster extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealBroadcaster.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node notificationExchangeNode = context.getConfigManager().getNotificationExchangeNode();
        try {
            for (Message msg : context.getMessages()) {
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);
                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      notificationExchangeNode.getRoutingKey(),
                                      msg.getContent(),
                                      properties);
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "RealBroadcaster");
            throw new RuntimeException(e);
        }
    }
}

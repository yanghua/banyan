package com.freedom.messagebus.client.handler.consumer;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * the real consumer
 */
public class RealConsumer extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealConsumer.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        Channel channel = context.getChannel();

        QueueingConsumer consumer = new QueueingConsumer(channel);
        try {
            channel.basicConsume(context.getRuleValue(), false, consumer);
        } catch (IOException e) {
            logger.error("[handler] occurs a IOException : " + e.getMessage());
        }

        //add external params
        context.getOtherParams().put("consumer", consumer);

        chain.handle(context);
    }
}

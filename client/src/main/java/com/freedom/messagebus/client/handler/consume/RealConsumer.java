package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
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
        if (!context.isSync()) {
            QueueingConsumer consumer = null;
            try {
                consumer = ProxyConsumer.consume(context.getChannel(),
                                                 context.getTargetNode().getValue(),
                                                 context.getConsumerTag());
            } catch (IOException e) {
                logger.error("[handler] occurs a IOException : " + e.getMessage());
            }

            //add external params
            context.getOtherParams().put("consumer", consumer);
        }

        chain.handle(context);
    }
}

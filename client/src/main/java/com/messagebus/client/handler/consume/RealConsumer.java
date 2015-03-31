package com.messagebus.client.handler.consume;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public void handle(MessageContext context, IHandlerChain chain) {
        if (!context.isSync()) {
            QueueingConsumer consumer = null;
            try {
                consumer = ProxyConsumer.consume(context.getChannel(),
                                                 context.getSourceNode().getValue(),
                                                 context.getConsumerTag());
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "real consumer");
                throw new RuntimeException(e);
            }

            //add external params
            context.getOtherParams().put("consumer", consumer);
        }

        chain.handle(context);
    }
}

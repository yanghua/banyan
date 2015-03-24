package com.messagebus.client.handler.request;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.rabbitmq.QueueManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class TempQueueInitializer extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(TempQueueInitializer.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        String correlationId = context.getSourceNode().getName();
        context.getMessages()[0].setCorrelationId(correlationId);
        QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
        try {
            queueManager.create(correlationId);
            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "[TempQueueInitializer]");
            throw new RuntimeException(e);
        }
    }
}

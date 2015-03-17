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
        long msgId = context.getMessages()[0].getMessageHeader().getMessageId();
        context.getMessages()[0].getMessageHeader().setCorrelationId(String.valueOf(msgId));
        QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
        try {
            queueManager.create(String.valueOf(msgId));
            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "[TempQueueInitializer]");
            throw new RuntimeException(e);
        }
    }
}

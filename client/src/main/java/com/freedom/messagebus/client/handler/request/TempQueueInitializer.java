package com.freedom.messagebus.client.handler.request;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.interactor.rabbitmq.QueueManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

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
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        long msgId = context.getMessages()[0].getMessageHeader().getMessageId();
        QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
        try {
            queueManager.create(String.valueOf(msgId));
            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        }
    }
}

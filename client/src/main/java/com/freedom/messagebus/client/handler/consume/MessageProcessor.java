package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.IReceiverCloser;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;

/**
 * message processor. it will trigger the receiver listener's onMessage event
 */
public class MessageProcessor extends AbstractHandler {

    private MessageContext context;

    private IReceiverCloser consumerCloser = new IReceiverCloser() {
        @Override
        public void close() {
            synchronized (this) {
                if (context.getReceiveEventLoop().isAlive()) {
                    context.getReceiveEventLoop().shutdown();
                }
            }
        }
    };

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context,
                       IHandlerChain chain) {
        if (!context.isSync() && context.getConsumedMsg() != null) {
            this.context = context;
            IMessageReceiveListener receiveListener = context.getListener();
            receiveListener.onMessage(context.getConsumedMsg(), consumerCloser);
        }

        chain.handle(context);
    }

}

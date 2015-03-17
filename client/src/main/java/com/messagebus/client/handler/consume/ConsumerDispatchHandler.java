package com.messagebus.client.handler.consume;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;

/**
 * Created by yanghua on 2/22/15.
 */
public class ConsumerDispatchHandler extends AbstractHandler {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (context.isSync()) {
            new SyncConsumerHandler().handle(context, chain);
        } else {
            new ConsumeLoopHandler().handle(context, chain);
        }
    }
}

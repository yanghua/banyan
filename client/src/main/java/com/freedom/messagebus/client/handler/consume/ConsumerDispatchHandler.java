package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;

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

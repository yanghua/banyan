package com.freedom.messagebus.client.handler;

import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.model.MessageCarryType;

import java.util.List;

/**
 * the default implementation of IHandlerChain
 * it handles both produce and consume process-chain
 */
public class MessageCarryHandlerChain implements IHandlerChain {

    private List<AbstractHandler> handlerChain;
    private int     pos          = 0;

    public MessageCarryHandlerChain(MessageCarryType type, GenericContext context) {
        switch (type) {
            case PRODUCE:
                handlerChain = context.getConfigManager().getProduceHandlerChain();
                break;

            case CONSUME:
                handlerChain = context.getConfigManager().getConsumerHandlerChain();
                break;

            case REQUEST:
                handlerChain = context.getConfigManager().getRequestHandlerChain();
                break;

            case RESPONSE:
                handlerChain = context.getConfigManager().getResponseHandlerChain();
                break;
        }
    }

    /**
     * the main process method
     *
     * @param context the message context
     */
    @Override
    public void handle(MessageContext context) {
        if (this.pos < handlerChain.size()) {
            AbstractHandler currentHandler = handlerChain.get(pos++);
            currentHandler.handle(context, this);
        }
    }
}

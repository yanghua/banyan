package com.freedom.messagebus.client.handler;

import com.freedom.messagebus.client.GenericContext;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.model.MessageCarryType;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * the default implementation of IHandlerChain
 * it handles both produce and consume process-chain
 */
public class MessageCarryHandlerChain implements IHandlerChain {

    private List<AbstractHandler> handlerChain;

    private int pos = 0;

    public MessageCarryHandlerChain(MessageCarryType type, GenericContext genericContext) {
        switch (type) {
            case PRODUCE:
                handlerChain = genericContext.getConfigManager().getProduceHandlerChain();
                break;

            case CONSUME:
                handlerChain = genericContext.getConfigManager().getConsumeHandlerChain();
                break;

            case REQUEST:
                handlerChain = genericContext.getConfigManager().getRequestHandlerChain();
                break;

            case RESPONSE:
                handlerChain = genericContext.getConfigManager().getResponseHandlerChain();
                break;

            case PUBLISH:
                handlerChain = genericContext.getConfigManager().getPublishHandlerChain();
                break;

            case SUBSCRIBE:
                handlerChain = genericContext.getConfigManager().getSubscribeHandlerChain();
                break;

            case BROADCAST:
                handlerChain = genericContext.getConfigManager().getBroadcastHandlerChain();
                break;

            default:
                throw new InvalidParameterException("invalid message carry type : " + type.toString());
        }
    }

    /**
     * the main process method
     *
     * @param context the message context
     */
    @Override
    public void handle(MessageContext context) {
        if (this.pos < this.handlerChain.size()) {
            AbstractHandler currentHandler = handlerChain.get(pos++);
            currentHandler.handle(context, this);
        }
    }

}

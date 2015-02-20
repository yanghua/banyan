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

    private List<AbstractHandler> preHandlerChain;
    private List<AbstractHandler> postHandlerChain;

    private int prePos  = 0;
    private int postPos = 0;

    private boolean preing  = false;
    private boolean posting = false;

    public MessageCarryHandlerChain(MessageCarryType type, GenericContext context) {
        switch (type) {
            case PRODUCE:
                preHandlerChain = context.getConfigManager().getPreProduceHandlerChain();
                postHandlerChain = context.getConfigManager().getPostProduceHandlerChain();
                break;

            case CONSUME:
                preHandlerChain = context.getConfigManager().getPreConsumeHandlerChain();
                postHandlerChain = context.getConfigManager().getPostConsumeHandlerChain();
                break;

            case REQUEST:
                preHandlerChain = context.getConfigManager().getPreRequestHandlerChain();
                postHandlerChain = context.getConfigManager().getPostRequestHandlerChain();
                break;

            case RESPONSE:
                preHandlerChain = context.getConfigManager().getPreResponseHandlerChain();
                postHandlerChain = context.getConfigManager().getPostResponseHandlerChain();
                break;

            case PUBLISH:
                preHandlerChain = context.getConfigManager().getPrePublishHandlerChain();
                postHandlerChain = context.getConfigManager().getPostPublishHandlerChain();
                break;

            case SUBSCRIBE:
                preHandlerChain = context.getConfigManager().getPreSubscribeHandlerChain();
                postHandlerChain = context.getConfigManager().getPostSubscribeHandlerChain();
                break;

            case BROADCAST:
                preHandlerChain = context.getConfigManager().getPreBroadcastHandlerChain();
                postHandlerChain = context.getConfigManager().getPostBroadcastHandlerChain();
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
        if (this.preing) {
            if (this.prePos < this.preHandlerChain.size()) {
                AbstractHandler currentHandler = preHandlerChain.get(prePos++);
                currentHandler.handle(context, this);
            }
        }

        if (this.posting) {
            if (this.postPos < this.postHandlerChain.size()) {
                AbstractHandler currentHandler = postHandlerChain.get(postPos++);
                currentHandler.handle(context, this);
            }
        }
    }

    public void startPre() {
        this.preing = true;
        this.posting = false;
    }

    public void startPost() {
        this.preing = false;
        this.posting = true;
    }

}

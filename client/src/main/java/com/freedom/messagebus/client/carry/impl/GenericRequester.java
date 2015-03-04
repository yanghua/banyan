package com.freedom.messagebus.client.carry.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.MessageResponseTimeoutException;
import com.freedom.messagebus.client.carry.IRequester;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenericRequester extends AbstractMessageCarryer implements IRequester {

    private static final Log logger = LogFactory.getLog(GenericRequester.class);

    public GenericRequester() {
    }

    /**
     * send a request and got a response
     *
     * @param msg     request message
     * @param to      send to destination
     * @param timeout response wait timeout
     * @return Message the response
     * @throws com.freedom.messagebus.client.MessageResponseTimeoutException
     */
    @Override
    public Message request(Message msg,
                           String to,
                           long timeout) throws MessageResponseTimeoutException {
        MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.REQUEST);
        ctx.setSourceNode(this.getContext().getConfigManager().getAppIdQueueMap().get(this.getContext().getAppId()));
        Node node = this.getContext().getConfigManager().getQueueNodeMap().get(to);
        ctx.setTargetNode(node);
        ctx.setTimeout(timeout);
        ctx.setMessages(new Message[]{msg});

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.REQUEST, this.getContext());
        //launch pre pipeline
        this.handlerChain.handle(ctx);

        if (ctx.isTimeout() || ctx.getConsumedMsg() == null)
            throw new MessageResponseTimeoutException("message request time out.");

        return ctx.getConsumedMsg();
    }


}

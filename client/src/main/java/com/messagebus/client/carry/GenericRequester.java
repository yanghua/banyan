package com.messagebus.client.carry;

import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 class GenericRequester extends AbstractMessageCarryer implements IRequester {

    private static final Log logger = LogFactory.getLog(GenericRequester.class);

    public GenericRequester() {
    }

    /**
     * send a request and got a response
     *
     * @param secret
     * @param to      send to destination
     * @param msg     request message
     * @param token
     * @param timeout response wait timeout  @return Message the response
     * @throws com.messagebus.client.MessageResponseTimeoutException
     */
    @Override
    public Message request(String secret, String to, Message msg,
                           String token, long timeout) throws MessageResponseTimeoutException {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setToken(token);
        ctx.setCarryType(MessageCarryType.REQUEST);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(secret));
        Node node = this.getContext().getConfigManager().getReqrespNodeMap().get(to);
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

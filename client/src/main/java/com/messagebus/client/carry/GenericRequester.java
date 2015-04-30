package com.messagebus.client.carry;

import com.messagebus.client.MessageContext;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.RpcClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
        ctx.setSourceNode(this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue());
        Node node = this.getContext().getConfigManager().getNodeView(secret).getRelatedQueueNameNodeMap().get(to);
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

    @Override
    public byte[] primitiveRequest(String secret, String target, byte[] requestMsg, String token, long timeoutOfMilliSecond) {
        Node sourceNode = this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue();
        Node targetNode = this.getContext().getConfigManager().getNodeView(secret).getRelatedQueueNameNodeMap().get(target);

        RpcClient innerRpcClient = null;
        try {
            innerRpcClient = new RpcClient(this.getContext().getChannel(), "exchange.proxy", targetNode.getRoutingKey(), (int) timeoutOfMilliSecond);
            return innerRpcClient.primitiveCall(requestMsg);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "primitive request ");
        } catch (TimeoutException e) {
            logger.info("primitiveRequest timeout : " + "[secret] " + secret + " [target] " + target);
        } finally {
            try {
                if (innerRpcClient != null) innerRpcClient.close();
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "primitive request finally close inner rpc client");
            }
        }

        return new byte[0];
    }


}

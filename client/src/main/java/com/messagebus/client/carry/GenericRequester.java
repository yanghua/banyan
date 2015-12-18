package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.event.carry.RequestEventProcessor;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.common.Constants;
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
        ctx.setSource(this.getContext().getConfigManager().getSourceBySecret(secret));
        ctx.setStream(this.getContext().getConfigManager().getStreamByToken(token));
        ConfigManager.Sink sink = this.getContext().getConfigManager().getSinkByName(to);
        ctx.setSink(sink);
        ctx.setTimeout(timeout);
        ctx.setMessages(new Message[]{msg});

        this.innerRequest(ctx);

        if (ctx.isTimeout() || ctx.getConsumeMsgs() == null || ctx.getConsumeMsgs().size() == 0)
            throw new MessageResponseTimeoutException("message request time out.");

        return ctx.getConsumeMsgs().get(0);
    }

    @Override
    public byte[] primitiveRequest(String secret,
                                   String target,
                                   byte[] requestMsg,
                                   String token,
                                   long timeoutOfMilliSecond) {
        ConfigManager.Source source = this.getContext().getConfigManager().getSourceBySecret(secret);
        ConfigManager.Sink   sink   = this.getContext().getConfigManager().getSinkByName(target);

        RpcClient innerRpcClient = null;
        try {
            innerRpcClient = new RpcClient(this.getContext().getChannel(),
                    Constants.PROXY_EXCHANGE_NAME,
                    sink.getRoutingKey(),
                    (int) timeoutOfMilliSecond);
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


    private void innerRequest(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        RequestEventProcessor eventProcessor = new RequestEventProcessor();
        carryEventBus.register(eventProcessor);

        RequestEventProcessor.ValidateEvent                validateEvent                = new RequestEventProcessor.ValidateEvent();
        CommonEventProcessor.MsgBodySizeCheckEvent         msgBodySizeCheckEvent        = new CommonEventProcessor.MsgBodySizeCheckEvent();
        RequestEventProcessor.PermissionCheckEvent         permissionCheckEvent         = new RequestEventProcessor.PermissionCheckEvent();
        CommonEventProcessor.MsgIdGenerateEvent            msgIdGenerateEvent           = new CommonEventProcessor.MsgIdGenerateEvent();
        RequestEventProcessor.TempQueueInitializeEvent     tempQueueInitializeEvent     = new RequestEventProcessor.TempQueueInitializeEvent();
        CommonEventProcessor.MsgBodyCompressEvent          msgBodyCompressEvent         = new CommonEventProcessor.MsgBodyCompressEvent();
        RequestEventProcessor.RequestEvent                 requestEvent                 = new RequestEventProcessor.RequestEvent();
        CommonEventProcessor.TagGenerateEvent              tagGenerateEvent             = new CommonEventProcessor.TagGenerateEvent();
        RequestEventProcessor.BlockAndTimeoutResponseEvent blockAndTimeoutResponseEvent = new RequestEventProcessor.BlockAndTimeoutResponseEvent();

        validateEvent.setMessageContext(ctx);
        msgBodySizeCheckEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        msgIdGenerateEvent.setMessageContext(ctx);
        tempQueueInitializeEvent.setMessageContext(ctx);
        msgBodyCompressEvent.setMessageContext(ctx);
        requestEvent.setMessageContext(ctx);
        tagGenerateEvent.setMessageContext(ctx);
        blockAndTimeoutResponseEvent.setMessageContext(ctx);

        //arrange event order and emit
        carryEventBus.post(validateEvent);
        carryEventBus.post(msgBodySizeCheckEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(msgIdGenerateEvent);
        carryEventBus.post(tempQueueInitializeEvent);
        carryEventBus.post(msgBodyCompressEvent);
        carryEventBus.post(requestEvent);
        carryEventBus.post(tagGenerateEvent);
        carryEventBus.post(blockAndTimeoutResponseEvent);

        //unregister
        carryEventBus.unregister(eventProcessor);
    }

}

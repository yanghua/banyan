package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.ProduceEventProcessor;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a generic producer implements the IProducer interface
 */
class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);

    /**
     * simple producer just produces a message
     *
     * @param secret
     * @param to     the message's destination
     * @param msg    a general message
     * @param token
     */
    @Override
    public void produce(String secret, String to, Message msg, String token) {
        MessageContext ctx = this.initMsgContext(secret, to, token);
        ctx.setMessages(new Message[]{msg});
        innerProduce(ctx);
    }

    /**
     * a producer produces a set of messages
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    @Override
    public void batchProduce(String secret, String to, Message[] msgs, String token) {
        MessageContext context = this.initMsgContext(secret, to, token);
        context.setMessages(msgs);
        innerProduce(context);
    }

    private MessageContext initMsgContext(String secret, String to, String token) {
        MessageContext context = initMessageContext();
        context.setSecret(secret);
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setSource(this.getContext().getConfigManager().getSourceBySecret(secret));
        ConfigManager.Sink sink = this.getContext().getConfigManager().getSinkByName(to);
        context.setSink(sink);
        context.setToken(token);
        context.setStream(this.getContext().getConfigManager().getStreamByToken(token));

        return context;
    }

    private void innerProduce(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        ProduceEventProcessor eventProcessor = new ProduceEventProcessor();
        carryEventBus.register(eventProcessor);

        //init events
        ProduceEventProcessor.ValidateEvent validateEvent = new ProduceEventProcessor.ValidateEvent();
        ProduceEventProcessor.MsgBodySizeCheckEvent msgBodySizeCheckEvent = new ProduceEventProcessor.MsgBodySizeCheckEvent();
        ProduceEventProcessor.PermissionCheckEvent permissionCheckEvent = new ProduceEventProcessor.PermissionCheckEvent();
        ProduceEventProcessor.MsgIdGenerateEvent msgIdGenerateEvent = new ProduceEventProcessor.MsgIdGenerateEvent();
        ProduceEventProcessor.MsgBodyCompressEvent msgBodyCompressEvent = new ProduceEventProcessor.MsgBodyCompressEvent();
        ProduceEventProcessor.ProduceEvent produceEvent = new ProduceEventProcessor.ProduceEvent();

        validateEvent.setMessageContext(ctx);
        msgBodySizeCheckEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        msgIdGenerateEvent.setMessageContext(ctx);
        msgBodyCompressEvent.setMessageContext(ctx);
        produceEvent.setMessageContext(ctx);

        //arrange event order and emit!
        carryEventBus.post(validateEvent);
        carryEventBus.post(msgBodySizeCheckEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(msgIdGenerateEvent);
        carryEventBus.post(msgBodyCompressEvent);
        carryEventBus.post(produceEvent);

        //unregister event processor
        carryEventBus.unregister(eventProcessor);
    }

}

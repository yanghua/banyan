package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.event.carry.SubscribeEventProcessor;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

class GenericSubscriber extends AbstractMessageCarryer implements ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    public GenericSubscriber() {
    }

    @Override
    public void subscribe(String secret,
                          String from, String token,
                          IMessageReceiveListener onMessage,
                          long timeout, TimeUnit unit) {
        final MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.SUBSCRIBE);
        ctx.setSecret(secret);
        ctx.setToken(token);
        ctx.setSource(this.getContext().getConfigManager().getSourceByName(from));
        ctx.setSink(this.getContext().getConfigManager().getSinkBySecret(secret));
        ctx.setStream(this.getContext().getConfigManager().getStreamByToken(token));
        ctx.setReceiveListener(onMessage);
        ctx.setSync(false);
        ctx.setTimeout(timeout);
        ctx.setTimeoutUnit(unit);

        this.innerSubscribe(ctx);
    }

    private void innerSubscribe(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        SubscribeEventProcessor eventProcessor = new SubscribeEventProcessor();
        carryEventBus.register(eventProcessor);

        //init events
        CommonEventProcessor.TagGenerateEvent        tagGenerateEvent      = new CommonEventProcessor.TagGenerateEvent();
        SubscribeEventProcessor.ValidateEvent        validateEvent         = new SubscribeEventProcessor.ValidateEvent();
        SubscribeEventProcessor.PermissionCheckEvent permissionCheckEvent  = new SubscribeEventProcessor.PermissionCheckEvent();
        CommonEventProcessor.PreConsumeEvent         perConsumeEvent       = new CommonEventProcessor.PreConsumeEvent();
        CommonEventProcessor.AsyncMessageLoopEvent   asyncMessageLoopEvent = new CommonEventProcessor.AsyncMessageLoopEvent();

        tagGenerateEvent.setMessageContext(ctx);
        validateEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        perConsumeEvent.setMessageContext(ctx);
        asyncMessageLoopEvent.setMessageContext(ctx);

        carryEventBus.post(tagGenerateEvent);
        carryEventBus.post(validateEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(perConsumeEvent);
        carryEventBus.post(asyncMessageLoopEvent);

        carryEventBus.unregister(eventProcessor);
    }

}

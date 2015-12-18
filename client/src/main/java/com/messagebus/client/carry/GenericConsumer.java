package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.event.carry.ConsumeEventProcessor;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 6/26/15.
 */
class GenericConsumer extends AbstractMessageCarryer implements IConsumer {

    private static final Log logger = LogFactory.getLog(GenericConsumer.class);

    @Override
    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage) {
        logger.debug("current secret is : " + secret);
        final MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setSink(this.getContext().getConfigManager().getSinkBySecret(secret));
        ctx.setReceiveListener(onMessage);
        ctx.setTimeout(timeout);
        ctx.setTimeoutUnit(unit);
        ctx.setSync(false);

        this.innerConsume(ctx);
    }

    @Override
    public List<Message> consume(String secret, int expectedNum) {
        final MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setSink(this.getContext().getConfigManager().getSinkBySecret(secret));
        ctx.setConsumeMsgNum(expectedNum);
        ctx.setSync(true);

        this.innerConsume(ctx);

        return ctx.getConsumeMsgs();
    }

    private void innerConsume(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        ConsumeEventProcessor eventProcessor = new ConsumeEventProcessor();
        carryEventBus.register(eventProcessor);

        //init events
        ConsumeEventProcessor.ValidateEvent        validateEvent        = new ConsumeEventProcessor.ValidateEvent();
        ConsumeEventProcessor.PermissionCheckEvent permissionCheckEvent = new ConsumeEventProcessor.PermissionCheckEvent();
        ConsumeEventProcessor.TagGenerateEvent     tagGenerateEvent     = new ConsumeEventProcessor.TagGenerateEvent();
        CommonEventProcessor.PreConsumeEvent       perConsumeEvent      = new CommonEventProcessor.PreConsumeEvent();

        validateEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        tagGenerateEvent.setMessageContext(ctx);
        perConsumeEvent.setMessageContext(ctx);

        //arrange event order and emit
        carryEventBus.post(validateEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(tagGenerateEvent);
        carryEventBus.post(perConsumeEvent);

        if (ctx.isSync()) {
            ConsumeEventProcessor.SyncConsumeEvent syncConsumeEvent = new ConsumeEventProcessor.SyncConsumeEvent();
            syncConsumeEvent.setMessageContext(ctx);
            carryEventBus.post(syncConsumeEvent);
        } else {
            ConsumeEventProcessor.AsyncMessageLoopEvent asyncMessageLoopEvent = new ConsumeEventProcessor.AsyncMessageLoopEvent();
            asyncMessageLoopEvent.setMessageContext(ctx);
            carryEventBus.post(asyncMessageLoopEvent);
        }

        carryEventBus.unregister(eventProcessor);
    }

}

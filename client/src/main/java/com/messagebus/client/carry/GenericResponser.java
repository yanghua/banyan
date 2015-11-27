package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.event.carry.ResponseEventProcessor;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

class GenericResponser extends AbstractMessageCarryer implements IResponser {

    private static final Log logger = LogFactory.getLog(GenericResponser.class);

    @Override
    public void response(String secret, IRequestListener onRequest, long timeout, TimeUnit timeUnit) {
        final MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.RESPONSE);
        ctx.setSecret(secret);
        ctx.setSink(this.getContext().getConfigManager().getSinkBySecret(secret));
        ctx.setRequestListener(onRequest);
        ctx.setTimeout(timeout);
        ctx.setTimeoutUnit(timeUnit);

        this.innerResponse(ctx);
    }

    private void innerResponse(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        ResponseEventProcessor eventProcessor = new ResponseEventProcessor();
        carryEventBus.register(eventProcessor);

        CommonEventProcessor.TagGenerateEvent tagGenerateEvent = new CommonEventProcessor.TagGenerateEvent();
        ResponseEventProcessor.ValidateEvent validateEvent = new ResponseEventProcessor.ValidateEvent();
        ResponseEventProcessor.PermissionCheckEvent permissionCheckEvent = new ResponseEventProcessor.PermissionCheckEvent();
        CommonEventProcessor.PreConsumeEvent perConsumeEvent = new CommonEventProcessor.PreConsumeEvent();
        CommonEventProcessor.AsyncMessageLoopEvent asyncMessageLoopEvent = new CommonEventProcessor.AsyncMessageLoopEvent();

        tagGenerateEvent.setMessageContext(ctx);
        validateEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        perConsumeEvent.setMessageContext(ctx);
        asyncMessageLoopEvent.setMessageContext(ctx);

        //arrange event order and emit
        carryEventBus.post(tagGenerateEvent);
        carryEventBus.post(validateEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(perConsumeEvent);
        carryEventBus.post(asyncMessageLoopEvent);

        //unregister
        carryEventBus.unregister(eventProcessor);
    }

}

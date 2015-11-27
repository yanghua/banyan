package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.BroadcastEventProcessor;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class GenericBroadcaster extends AbstractMessageCarryer implements IBroadcaster {

    private static final Log logger = LogFactory.getLog(GenericBroadcaster.class);

    public GenericBroadcaster() {
    }

    @Override
    public void broadcast(String secret, Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.BROADCAST);
        ctx.setSource(this.getContext().getConfigManager().getSourceBySecret(secret));
        ctx.setMessages(msgs);

        this.innerBroadcast(ctx);
    }

    private void innerBroadcast(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        BroadcastEventProcessor eventProcessor = new BroadcastEventProcessor();
        carryEventBus.register(eventProcessor);

        CommonEventProcessor.MsgIdGenerateEvent msgIdGenerateEvent = new CommonEventProcessor.MsgIdGenerateEvent();
        BroadcastEventProcessor.ValidateEvent validateEvent = new BroadcastEventProcessor.ValidateEvent();
        BroadcastEventProcessor.PermissionCheckEvent permissionCheckEvent = new BroadcastEventProcessor.PermissionCheckEvent();
        BroadcastEventProcessor.BroadcastEvent broadcastEvent = new BroadcastEventProcessor.BroadcastEvent();

        msgIdGenerateEvent.setMessageContext(ctx);
        validateEvent.setMessageContext(ctx);
        permissionCheckEvent.setMessageContext(ctx);
        broadcastEvent.setMessageContext(ctx);

        carryEventBus.post(msgIdGenerateEvent);
        carryEventBus.post(validateEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(broadcastEvent);

        carryEventBus.unregister(eventProcessor);
    }

}

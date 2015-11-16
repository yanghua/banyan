package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.CommonEventProcessor;
import com.messagebus.client.event.carry.PublishEventProcessor;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class GenericPublisher extends AbstractMessageCarryer implements IPublisher {

    private static final Log logger = LogFactory.getLog(GenericPublisher.class);

    public GenericPublisher() {
    }

    @Override
    public void publish(String secret, Message[] msgs) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.PUBLISH);
        ctx.setSourceNode(this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue());
        ctx.setMessages(msgs);

        this.innerPublish(ctx);
    }

    private void innerPublish(MessageContext context) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        PublishEventProcessor eventProcessor = new PublishEventProcessor();
        carryEventBus.register(eventProcessor);

        //init events
        CommonEventProcessor.MsgIdGenerateEvent msgIdGenerateEvent = new CommonEventProcessor.MsgIdGenerateEvent();
        PublishEventProcessor.ValidateEvent validateEvent = new PublishEventProcessor.ValidateEvent();
        PublishEventProcessor.PermissionCheckEvent permissionCheckEvent = new PublishEventProcessor.PermissionCheckEvent();
        PublishEventProcessor.PublishEvent publishEvent = new PublishEventProcessor.PublishEvent();

        msgIdGenerateEvent.setMessageContext(context);
        validateEvent.setMessageContext(context);
        permissionCheckEvent.setMessageContext(context);
        publishEvent.setMessageContext(context);

        //arrange event order and emit
        carryEventBus.post(msgIdGenerateEvent);
        carryEventBus.post(validateEvent);
        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(publishEvent);

        //unregister
        carryEventBus.unregister(eventProcessor);
    }

}

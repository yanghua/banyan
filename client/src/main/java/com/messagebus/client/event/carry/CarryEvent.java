package com.messagebus.client.event.carry;

import com.messagebus.client.MessageContext;
import com.messagebus.common.Event;

/**
 * Created by yanghua on 6/25/15.
 */
public class CarryEvent extends Event {

    private MessageContext messageContext;

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }
}

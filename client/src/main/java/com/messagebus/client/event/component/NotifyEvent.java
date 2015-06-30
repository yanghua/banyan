package com.messagebus.client.event.component;

import com.messagebus.client.message.model.Message;
import com.messagebus.common.Event;

/**
 * Created by yanghua on 6/29/15.
 */
public class NotifyEvent extends Event {

    private Message msg;

    public NotifyEvent() {
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message notification) {
        this.msg = notification;
    }
}

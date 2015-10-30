package com.messagebus.client.event.component;

import com.messagebus.client.message.model.Message;
import com.messagebus.common.Event;

/**
 * Created by yanghua on 10/28/15.
 */
public class InnerEvent extends Event {

    private Message msg;

    public InnerEvent() {
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }
}

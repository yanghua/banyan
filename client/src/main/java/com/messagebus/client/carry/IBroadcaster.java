package com.messagebus.client.carry;

import com.messagebus.client.message.model.Message;

public interface IBroadcaster {

    public void broadcast(String secret, Message[] msgs);

}

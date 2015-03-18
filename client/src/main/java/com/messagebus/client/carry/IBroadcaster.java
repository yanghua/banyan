package com.messagebus.client.carry;

import com.messagebus.client.message.model.IMessage;

public interface IBroadcaster {

    public void broadcast(String secret, IMessage[] msgs, String token);

}

package com.freedom.messagebus.client;

import com.freedom.messagebus.client.message.model.Message;

public interface IBroadcaster {

    public void broadcast(Message[] msgs);

}

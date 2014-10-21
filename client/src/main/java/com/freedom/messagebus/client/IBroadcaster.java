package com.freedom.messagebus.client;

import com.freedom.messagebus.common.message.Message;

public interface IBroadcaster {

    public void broadcast(Message[] msgs);

}

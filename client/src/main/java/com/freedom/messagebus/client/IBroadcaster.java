package com.freedom.messagebus.client;

import com.freedom.messagebus.business.message.model.Message;

public interface IBroadcaster {

    public void broadcast(Message[] msgs);

}

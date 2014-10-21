package com.freedom.messagebus.client;

import com.freedom.messagebus.common.message.Message;

public interface IPublisher {

    public void publish(Message[] msgs);

}

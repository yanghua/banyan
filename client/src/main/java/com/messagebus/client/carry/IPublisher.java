package com.messagebus.client.carry;

import com.messagebus.client.message.model.Message;

public interface IPublisher {

    public void publish(String secret, Message[] msgs);

}

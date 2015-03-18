package com.messagebus.client.carry;

import com.messagebus.client.message.model.IMessage;

public interface IPublisher {

    public void publish(String secret, IMessage[] msgs, String token);

}

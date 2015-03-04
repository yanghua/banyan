package com.freedom.messagebus.client.carry;

import com.freedom.messagebus.client.message.model.Message;

public interface IPublisher {

    public void publish(Message[] msgs);

}

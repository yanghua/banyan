package com.freedom.messagebus.client;

import com.freedom.messagebus.client.message.model.Message;

public interface IPublisher extends IBasicOperator {

    public void publish(Message[] msgs);

}

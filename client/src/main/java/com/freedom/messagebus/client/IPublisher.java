package com.freedom.messagebus.client;

import com.freedom.messagebus.business.message.model.Message;

public interface IPublisher {

    public void publish(Message[] msgs);

}

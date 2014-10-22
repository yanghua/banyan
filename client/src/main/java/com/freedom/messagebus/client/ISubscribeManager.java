package com.freedom.messagebus.client;

public interface ISubscribeManager extends IReceiverCloser {

    @Override
    void close();

    void addSubscriber(String subQueueName);

    void removeSubscriber(String subQueueName);
}

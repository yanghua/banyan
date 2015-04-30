package com.messagebus.client.carry;

import com.messagebus.client.message.model.Message;

/**
 * the interface of producer
 */
public interface IProducer {

    public void produce(String secret, String to, Message msg, String token);

    public void batchProduce(String secret, String to, Message[] msgs, String token);

}

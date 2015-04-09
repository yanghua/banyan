package com.messagebus.client.carry;

import com.messagebus.client.message.model.Message;

/**
 * the interface of producer
 */
public interface IProducer {

    public void produce(String secret, String to, Message msg, String token);

    /**
     * a producer produces a set of messages
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    public void batchProduce(String secret, String to, Message[] msgs, String token);

}

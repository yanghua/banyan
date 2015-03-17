package com.messagebus.client.carry;

import com.messagebus.client.message.model.Message;

/**
 * the interface of producer
 */
public interface IProducer {

    /**
     * simple producer just produces a message
     *
     * @param secret
     * @param to     the message's destination
     * @param msg    a general message
     * @param token
     */
    public void produce(String secret, String to, Message msg, String token);


    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param secret
     * @param to     the message's destination
     * @param msg    a general message
     * @param token
     */
    public void produceWithTX(String secret, String to, Message msg, String token);


    /**
     * a producer produces a set of messages
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    public void batchProduce(String secret, String to, Message[] msgs, String token);


    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param secret
     * @param to     the message's destination
     * @param msgs   a general message's array
     * @param token
     */
    public void batchProduceWithTX(String secret, String to, Message[] msgs, String token);

}

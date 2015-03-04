package com.freedom.messagebus.client.carry;

import com.freedom.messagebus.client.message.model.Message;

/**
 * the interface of producer
 */
public interface IProducer {

    /**
     * simple producer just produces a message
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    public void produce(Message msg, String to);


    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg a general message
     * @param to  the message's destination
     */
    public void produceWithTX(Message msg, String to);


    /**
     * a producer produces a set of messages
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    public void batchProduce(Message[] msgs, String to);


    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs a general message's array
     * @param to   the message's destination
     */
    public void batchProduceWithTX(Message[] msgs, String to);

}

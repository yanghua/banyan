package com.freedom.messagebus.client;

import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.message.Message;
import org.jetbrains.annotations.NotNull;

/**
 * the interface of producer
 */
public interface IProducer {

    /**
     * simple producer just produces a message
     *
     * @param msg    a general message
     * @param to     the message's destination
     */
    public void produce(@NotNull Message msg, @NotNull String to);


    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg    a general message
     * @param to     the message's destination
     */
    public void produceWithTX(@NotNull Message msg, @NotNull String to);


    /**
     * a producer produces a set of messages
     *
     * @param msgs   a general message's array
     * @param to     the message's destination
     */
    public void batchProduce(@NotNull Message[] msgs, @NotNull String to);


    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs    a general message's array
     * @param to      the message's destination
     */
    public void batchProduceWithTX(@NotNull Message[] msgs, @NotNull String to);

}

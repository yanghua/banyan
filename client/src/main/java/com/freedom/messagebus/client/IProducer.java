package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.model.MessageFormat;
import org.jetbrains.annotations.NotNull;

/**
 * the interface of producer
 */
interface IProducer {

    /**
     * simple producer just produces a message
     *
     * @param msg       a general message
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    public void produce(@NotNull Message msg,
                        MessageFormat msgFormat,
                        @NotNull String appKey,
                        @NotNull String to,
                        @NotNull String msgType);


    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg       a general message
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    public void produceWithTX(@NotNull Message msg,
                              MessageFormat msgFormat,
                              @NotNull String appKey,
                              @NotNull String to,
                              @NotNull String msgType);


    /**
     * a producer produces a set of messages
     *
     * @param msgs      a general message's array
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    public void batchProduce(@NotNull Message[] msgs,
                             MessageFormat msgFormat,
                             @NotNull String appKey,
                             @NotNull String to,
                             @NotNull String msgType);


    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs      a general message's array
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    public void batchProduceWithTX(@NotNull Message[] msgs,
                                   MessageFormat msgFormat,
                                   @NotNull String appKey,
                                   @NotNull String to,
                                   @NotNull String msgType);

}

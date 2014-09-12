package com.freedom.messagebus.client;

/**
 * the interface of consumer's closer
 */
public interface IConsumerCloser {

    /**
     * close a consumer after a consumer won't consume continuously
     */
    public void closeConsumer();

}

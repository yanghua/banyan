package com.freedom.messagebus.client;

/**
 * the interface of consumer's closer
 */
public interface IReceiverCloser {

    /**
     * close a consumer after a consumer won't consume continuously
     */
    public void close();

}

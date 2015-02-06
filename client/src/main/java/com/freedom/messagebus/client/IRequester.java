package com.freedom.messagebus.client;

import com.freedom.messagebus.client.message.model.Message;

public interface IRequester extends IBasicOperator {

    /**
     * send a request and got a response
     *
     * @param msg     request message
     * @param to      send to destination
     * @param timeout response wait timeout
     * @return Message the response message
     * @throws MessageResponseTimeoutException
     */
    public Message request(Message msg, String to, long timeout)
        throws MessageResponseTimeoutException;

}

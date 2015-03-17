package com.messagebus.client.carry;

import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.message.model.Message;

public interface IRequester {

    /**
     * send a request and got a response
     *
     * @param secret
     * @param to      send to destination
     * @param msg     request message
     * @param token
     * @param timeout response wait timeout  @return Message the response message
     * @throws com.messagebus.client.MessageResponseTimeoutException
     */
    public Message request(String secret, String to, Message msg, String token, long timeout)
        throws MessageResponseTimeoutException;

}

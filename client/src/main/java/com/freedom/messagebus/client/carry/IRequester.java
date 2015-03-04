package com.freedom.messagebus.client.carry;

import com.freedom.messagebus.client.MessageResponseTimeoutException;
import com.freedom.messagebus.client.message.model.Message;

public interface IRequester {

    /**
     * send a request and got a response
     *
     * @param msg     request message
     * @param to      send to destination
     * @param timeout response wait timeout
     * @return Message the response message
     * @throws com.freedom.messagebus.client.MessageResponseTimeoutException
     */
    public Message request(Message msg, String to, long timeout)
        throws MessageResponseTimeoutException;

}

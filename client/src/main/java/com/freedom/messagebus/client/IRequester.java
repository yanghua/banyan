package com.freedom.messagebus.client;

import com.freedom.messagebus.business.message.model.Message;
import org.jetbrains.annotations.NotNull;

public interface IRequester {

    /**
     * send a request and got a response
     *
     * @param msg     request message
     * @param to      send to destination
     * @param timeout response wait timeout
     * @return Message the response message
     * @throws MessageResponseTimeoutException
     */
    public Message request(@NotNull Message msg, @NotNull String to, long timeout)
        throws MessageResponseTimeoutException;

}

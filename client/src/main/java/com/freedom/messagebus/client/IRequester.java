package com.freedom.messagebus.client;

import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.message.Message;
import org.jetbrains.annotations.NotNull;

public interface IRequester {

    /**
     * send a request and got a response
     * @param msg request message
     * @param to send to destination
     * @param timeout response wait timeout
     * @throws MessageResponseTimeoutException
     * @return Message the response message
     */
    public Message request(@NotNull Message msg, @NotNull String to, long timeout)
        throws MessageResponseTimeoutException;

}

package com.messagebus.client.carry;

import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.message.model.Message;

public interface IRequester {

    public Message request(String secret, String to, Message msg, String token, long timeout)
            throws MessageResponseTimeoutException;

    public byte[] primitiveRequest(String secret, String target, byte[] requestMsg, String token, long timeoutOfMilliSecond);


}

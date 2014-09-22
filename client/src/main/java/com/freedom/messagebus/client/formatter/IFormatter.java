package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.core.message.Message;

/**
 * the interface of formatter
 */
@Deprecated
public interface IFormatter {

    /**
     * format message to a byte array
     *
     * @param msg a generic message
     * @return formatted byte array
     */
    public byte[] format(Message msg);

    /**
     * deformat a message byte array to a generic message
     *
     * @param msgBytes a byte array
     * @return a generic message
     */
    public Message deFormat(byte[] msgBytes);

}

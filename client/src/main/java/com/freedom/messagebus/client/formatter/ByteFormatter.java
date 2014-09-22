package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.core.message.Message;

/**
 * the formatter for bytes
 */
@Deprecated
public class ByteFormatter implements IFormatter {

    /**
     * format message to a byte array
     *
     * @param msg a generic message
     * @return formatted byte array
     */
    @Override
    public byte[] format(Message msg) {
        return new byte[0];
    }

    /**
     * deformat a message byte array to a generic message
     *
     * @param msgBytes a byte array
     * @return a generic message
     */
    @Override
    public Message deFormat(byte[] msgBytes) {
        return null;
    }
}

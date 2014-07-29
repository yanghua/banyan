package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.core.message.Message;

/**
 * the stream formatter
 */
public class StreamFormatter implements IFormatter {

    @Override
    public byte[] format(Message msg) {
        return new byte[0];
    }

    @Override
    public Message deFormat(byte[] msgBytes) {
        return null;
    }
}

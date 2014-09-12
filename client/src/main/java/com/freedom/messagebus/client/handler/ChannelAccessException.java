package com.freedom.messagebus.client.handler;

/**
 * exception : channel access exception
 */
public class ChannelAccessException extends RuntimeException {

    public ChannelAccessException() {
        super();
    }

    public ChannelAccessException(String message) {
        super(message);
    }

    public ChannelAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelAccessException(Throwable cause) {
        super(cause);
    }
}

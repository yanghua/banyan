package com.freedom.messagebus.client.core.pool;

/**
 * the exception about channel connect
 */
public class ChannelConnectException extends RuntimeException {

    public ChannelConnectException() {
        super();
    }

    public ChannelConnectException(String message) {
        super(message);
    }

    public ChannelConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelConnectException(Throwable cause) {
        super(cause);
    }
}

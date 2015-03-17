package com.messagebus.client;

public class MessageResponseTimeoutException extends Exception {

    public MessageResponseTimeoutException() {
        super();
    }

    public MessageResponseTimeoutException(String message) {
        super(message);
    }

    public MessageResponseTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageResponseTimeoutException(Throwable cause) {
        super(cause);
    }

    protected MessageResponseTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

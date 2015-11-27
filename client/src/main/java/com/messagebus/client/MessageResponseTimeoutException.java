package com.messagebus.client;

public class MessageResponseTimeoutException extends RuntimeException {

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

}

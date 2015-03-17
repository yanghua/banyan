package com.messagebus.client;

/**
 * the exception about messagebus connect failed exception
 */
public class MessagebusConnectedFailedException extends Exception {

    public MessagebusConnectedFailedException() {
        super();
    }

    public MessagebusConnectedFailedException(String message) {
        super(message);
    }

    public MessagebusConnectedFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagebusConnectedFailedException(Throwable cause) {
        super(cause);
    }
}

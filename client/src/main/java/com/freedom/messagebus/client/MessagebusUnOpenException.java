package com.freedom.messagebus.client;

/**
 * the exception about messagebus unopened, mostly for semantic
 */
public class MessagebusUnOpenException extends RuntimeException {

    public MessagebusUnOpenException() {
        super();
    }

    public MessagebusUnOpenException(String message) {
        super(message);
    }

    public MessagebusUnOpenException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagebusUnOpenException(Throwable cause) {
        super(cause);
    }
}

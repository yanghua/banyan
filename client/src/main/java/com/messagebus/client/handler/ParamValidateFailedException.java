package com.messagebus.client.handler;

/**
 * exception : parameter validate failed exception
 */
public class ParamValidateFailedException extends RuntimeException {

    public ParamValidateFailedException() {
        super();
    }

    public ParamValidateFailedException(String message) {
        super(message);
    }

    public ParamValidateFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamValidateFailedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "ParamValidateFailedException";
    }
}

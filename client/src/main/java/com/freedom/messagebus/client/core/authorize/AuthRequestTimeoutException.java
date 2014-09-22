package com.freedom.messagebus.client.core.authorize;

/**
 * authorize request timeout exception
 */
public class AuthRequestTimeoutException extends Exception {

    public AuthRequestTimeoutException() {
        super();
    }

    public AuthRequestTimeoutException(String message) {
        super(message);
    }

    public AuthRequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthRequestTimeoutException(Throwable cause) {
        super(cause);
    }
}

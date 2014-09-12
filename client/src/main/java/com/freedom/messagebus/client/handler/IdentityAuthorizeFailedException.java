package com.freedom.messagebus.client.handler;

/**
 * exception : identity authorize failed exception
 */
public class IdentityAuthorizeFailedException extends RuntimeException {

    public IdentityAuthorizeFailedException(Throwable cause) {
        super(cause);
    }

    public IdentityAuthorizeFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityAuthorizeFailedException(String message) {
        super(message);
    }

    public IdentityAuthorizeFailedException() {
        super();
    }

    @Override
    public String toString() {
        return "IdentityAuthorizeFailedException";
    }
}

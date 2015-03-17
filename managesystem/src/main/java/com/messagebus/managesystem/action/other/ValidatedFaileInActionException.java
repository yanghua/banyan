package com.messagebus.managesystem.action.other;

public class ValidatedFaileInActionException extends RuntimeException {

    public ValidatedFaileInActionException() {
        super();
    }

    public ValidatedFaileInActionException(String message) {
        super(message);
    }

    public ValidatedFaileInActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidatedFaileInActionException(Throwable cause) {
        super(cause);
    }

    protected ValidatedFaileInActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

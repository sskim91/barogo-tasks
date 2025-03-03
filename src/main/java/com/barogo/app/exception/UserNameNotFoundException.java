package com.barogo.app.exception;

public class UserNameNotFoundException extends RuntimeException {

    public UserNameNotFoundException() {
    }

    public UserNameNotFoundException(final String message) {
        super(message);
    }

    public UserNameNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserNameNotFoundException(final Throwable cause) {
        super(cause);
    }

    public UserNameNotFoundException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package com.dasset.wallet.core.wallet.hd.crypto;

public class InsufficientBytesException extends Exception {

    public InsufficientBytesException(String message) {
        super(message);
    }

    public InsufficientBytesException(Exception exception) {
        super(exception);
    }

    public InsufficientBytesException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

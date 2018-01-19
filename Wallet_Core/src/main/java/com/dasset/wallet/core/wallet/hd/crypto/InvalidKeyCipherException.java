package com.dasset.wallet.core.wallet.hd.crypto;

public class InvalidKeyCipherException extends Exception {

    public InvalidKeyCipherException(String message) {
        super(message);
    }

    public InvalidKeyCipherException(Exception exception) {
        super(exception);
    }

    public InvalidKeyCipherException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

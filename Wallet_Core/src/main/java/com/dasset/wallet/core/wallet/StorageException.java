package com.dasset.wallet.core.wallet;

public class StorageException extends Exception {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Exception exception) {
        super(exception);
    }

    public StorageException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

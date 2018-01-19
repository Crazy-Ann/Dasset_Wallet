package com.dasset.wallet.core.wallet.hd.crypto;

public class SignatureException extends Exception {

    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(Exception exception) {
        super(exception);
    }

    public SignatureException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

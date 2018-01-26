package com.dasset.wallet.core.exception;

public class DuplicatedHDAccountException extends RuntimeException {

    public DuplicatedHDAccountException(String s) {
        super(s);
    }

    public DuplicatedHDAccountException(String s, Throwable throwable) {
        super(s, throwable);
    }
}

package com.pepijndejong.ssj.service.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {

    }

    public TokenNotFoundException(final Throwable e) {
        super(e);
    }
}

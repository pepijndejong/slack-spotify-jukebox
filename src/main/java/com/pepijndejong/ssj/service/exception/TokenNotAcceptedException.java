package com.pepijndejong.ssj.service.exception;

public class TokenNotAcceptedException extends RuntimeException {
    public TokenNotAcceptedException(final Throwable e) {
        super(e);
    }
}

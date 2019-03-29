package com.pepijndejong.ssj.service.exception;

public class SpotifyApiCallFailedException extends RuntimeException {

    public SpotifyApiCallFailedException(final Throwable cause) {
        super(cause);
    }

    public SpotifyApiCallFailedException(final String message) {
        super(message);
    }

}

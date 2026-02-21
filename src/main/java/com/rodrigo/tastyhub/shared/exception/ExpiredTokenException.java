package com.rodrigo.tastyhub.shared.exception;

public class ExpiredTokenException extends TokenException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}

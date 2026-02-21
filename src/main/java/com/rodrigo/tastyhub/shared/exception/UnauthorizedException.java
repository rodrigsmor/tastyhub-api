package com.rodrigo.tastyhub.shared.exception;

public class UnauthorizedException extends TokenException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

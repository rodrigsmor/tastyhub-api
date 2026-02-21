package com.rodrigo.tastyhub.exceptions;

public class UnauthorizedException extends TokenException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

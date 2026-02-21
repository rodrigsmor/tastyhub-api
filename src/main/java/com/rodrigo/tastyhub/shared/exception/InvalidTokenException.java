package com.rodrigo.tastyhub.shared.exception;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

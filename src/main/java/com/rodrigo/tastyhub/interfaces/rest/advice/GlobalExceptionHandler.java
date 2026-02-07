package com.rodrigo.tastyhub.interfaces.rest.advice;

import com.rodrigo.tastyhub.application.dto.response.ErrorResponseDto;
import com.rodrigo.tastyhub.exceptions.ExpiredTokenException;
import com.rodrigo.tastyhub.exceptions.InfrastructureException;
import com.rodrigo.tastyhub.exceptions.InvalidTokenException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorized(BadCredentialsException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralError(Exception ex) {
        ErrorResponseDto error = new ErrorResponseDto(
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<String> handleExpiredToken(ExpiredTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<String> handleInfrastructure(InfrastructureException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request or invalid fields";

        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException unrecognizedEx) {
            String propertyName = unrecognizedEx.getPropertyName();
            message = String.format("Unknown field detected: '%s'", propertyName);
        } else if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {
            message = String.format("Invalid format for field: '%s'", invalidEx.getPath().get(0).getFieldName());
        }

        ErrorResponseDto error = new ErrorResponseDto(
            message,
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

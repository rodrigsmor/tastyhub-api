package com.rodrigo.tastyhub.application.dto.response;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    String message,
    int status,
    LocalDateTime timestamp
) {}

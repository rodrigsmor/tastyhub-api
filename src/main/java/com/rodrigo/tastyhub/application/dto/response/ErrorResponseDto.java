package com.rodrigo.tastyhub.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


public record ErrorResponseDto(
    @Schema(
        description = "Error message detailing the cause of the failure",
        example = "Invalid email format",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String message,

    @Schema(
        description = "HTTP status code",
        example = "400",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int status,

    @Schema(
        description = "Timestamp when the error occurred",
        example = "2026-02-14T15:00:37",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDateTime timestamp
) {}
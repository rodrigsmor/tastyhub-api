package com.rodrigo.tastyhub.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupResponseDto (
    @Schema(
        description = "Success Message",
        example = "An email has been sent to the address you provided. Please check it to validate your account!"
    )
    String message,

    @Schema(
        description = "The email address provided during sign-up",
        example = "john.doe@example.com"
    )
    String emailSentTo
) {}

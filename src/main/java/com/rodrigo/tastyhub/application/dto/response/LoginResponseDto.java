package com.rodrigo.tastyhub.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto(
    @Schema(
        description = "JWT access token used for authenticated requests",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String accessToken,

    @Schema(
        description = "Token used to obtain a new access token when the current one expires",
        example = "d7b8f9e0-a1b2-c3d4-e5f6-7g8h9i0j1k2l",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String refreshToken,

    @Schema(
        description = "Type of the token generated",
        example = "Bearer",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String tokenType
) {}

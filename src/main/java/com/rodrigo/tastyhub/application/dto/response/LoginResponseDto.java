package com.rodrigo.tastyhub.application.dto.response;

public record LoginResponseDto(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    String tokenType
) {}

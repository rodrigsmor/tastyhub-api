package com.rodrigo.tastyhub.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record LoginRequestDto(
    @Schema(
        description = "User's registered email address",
        example = "john.doe@example.com",
        maxLength = 92,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 92, message = "Email must not exceed 92 characters")
    String email,

    @Schema(
        description = "Account password",
        example = "Password123!",
        minLength = 8,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) { }

package com.rodrigo.tastyhub.modules.auth.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record SignupRequestDto(
    @Schema(
        description = "User's first name",
        example = "John",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @Schema(
        description = "User's last name",
        example = "Doe",
        maxLength = 150,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Last name is required")
    @Size(max = 150, message = "Last name must not exceed 150 characters")
    String lastName,

    @Schema(
        description = "User's electronic mail address",
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
        example = "StrongPass123!",
        minLength = 8,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {}
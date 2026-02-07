package com.rodrigo.tastyhub.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 92, message = "Email must not exceed 92 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) { }

package com.rodrigo.tastyhub.application.dto.request;

import com.rodrigo.tastyhub.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SignupRequestDto(
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 150, message = "Last name must not exceed 150 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 92, message = "Email must not exceed 92 characters")
    String email,

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    String username,

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 280, message = "Bio must not exceed 280 characters")
    String bio,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,

    @NotEmpty(message = "At least one role must be assigned")
    Set<UserRole> roles
) {}
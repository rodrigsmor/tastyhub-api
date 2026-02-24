package com.rodrigo.tastyhub.modules.user.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OnboardingProfileRequest(
        @Schema(
        description = "Unique handle for the user profile",
        example = "chef_johndoe",
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username is required")
    @Size(max = 20, message = "Username must not exceed 20 characters")
    String username,

        @Schema(
        description = "Brief user biography or description",
        example = "Passionate home cook and food explorer.",
        maxLength = 280,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 280, message = "Bio must not exceed 280 characters")
    String bio,

        @Schema(
        description = "Alternative text for the profile image (accessibility)",
        example = "John Doe smiling in a kitchen setting",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String profilePictureAlt,

    @Schema(
        description = "Date of birth in ISO format",
        example = "1995-08-25",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Past(message = "The date of birth must be in the past")
    LocalDate dateOfBirth
) {}
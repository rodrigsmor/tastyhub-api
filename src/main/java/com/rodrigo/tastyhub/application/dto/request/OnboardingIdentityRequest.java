package com.rodrigo.tastyhub.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OnboardingIdentityRequest(
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
        description = "Public URL of the profile image",
        example = "https://cdn.example.com/profiles/johndoe.jpg",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String profilePictureUrl,

    @Schema(
        description = "Alternative text for the profile image (accessibility)",
        example = "John Doe smiling in a kitchen setting",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String profilePictureAlt
) {}
package com.rodrigo.tastyhub.modules.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Detailed user profile with engagement statistics and social metadata")
public record UserFullStatsDto(
    @Schema(description = "Unique identifier of the user", example = "1")
    Long id,

    @Schema(description = "User's first name", example = "Rodrigo")
    String firstName,

    @Schema(description = "User's last name", example = "Silva")
    String lastName,

    @Schema(description = "Unique public username", example = "rodrigo_chef")
    String username,

    @Schema(description = "URL for the profile picture (avatar)", example = "https://cdn.tastyhub.com/profiles/1.jpg")
    String profilePictureUrl,

    @Schema(description = "Alternative text for accessibility of the profile picture", example = "Rodrigo smiling in a kitchen")
    String profilePictureAlt,

    @Schema(description = "User's short biography or description", example = "Passionate home cook and food photographer.")
    String bio,

    @Schema(description = "URL for the profile header/cover image", example = "https://cdn.tastyhub.com/covers/1.jpg")
    String coverUrl,

    @Schema(description = "Alternative text for accessibility of the cover image", example = "A rustic wooden table with fresh ingredients")
    String coverAlt,

    @Schema(description = "User's date of birth (ISO 8601)", example = "1995-05-15")
    LocalDate dateOfBirth,

    @Schema(description = "Total number of recipes published by the user", example = "42")
    long recipeCount,

    @Schema(description = "Total number of articles or blog posts published", example = "12")
    long articleCount,

    @Schema(description = "Total number of followers", example = "1540")
    long followerCount,

    @Schema(description = "Total number of users being followed", example = "320")
    long followingCount
) {
    public UserFullStatsDto {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(username, "Username is required");
    }
}

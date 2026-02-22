package com.rodrigo.tastyhub.modules.user.application.dto.response;

import java.time.LocalDate;
import java.util.Objects;

public record UserFullStatsDto(
    Long id,
    String firstName,
    String lastName,
    String username,
    String profilePictureUrl,
    String profilePictureAlt,
    String bio,
    String coverUrl,
    String coverAlt,
    LocalDate dateOfBirth,
    long recipeCount,
    long articleCount,
    long followerCount,
    long followingCount
) {
    public UserFullStatsDto {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(username, "Username is required");
    }
}

package com.rodrigo.tastyhub.modules.user.application.dto.response;

import java.util.Objects;

public record UserProfileDto(
    Long id,
    String firstName,
    String lastName,
    String username,
    String profilePictureUrl,
    String profilePictureAlt,
    String bio,
    String coverUrl,
    String coverAlt
) {
    public UserProfileDto {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(username, "Username is required");
    }
}

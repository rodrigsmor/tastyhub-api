package com.rodrigo.tastyhub.modules.user.application.dto.response;

import java.util.Objects;

public record UserSummaryDto(
    Long id,
    String firstName,
    String lastName,
    String username,
    String profilePictureUrl,
    String profilePictureAlt
) {
    public UserSummaryDto {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(username, "Username is required");
    }
}

package com.rodrigo.tastyhub.modules.user.application.dto.response;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simplified representation of a user, typically used in search results or content authorship")
public record UserSummaryDto(

    @Schema(description = "The unique internal identifier of the user", example = "42")
    Long id,

    @Schema(description = "The user's given name", example = "John")
    String firstName,

    @Schema(description = "The user's family name", example = "Doe")
    String lastName,

    @Schema(description = "Unique public handle used for mentions and profile URLs", example = "chef_master_99")
    String username,

    @Schema(
        description = "Direct URL to the user's profile image",
        example = "https://cdn.tastyhub.com/profiles/u42_avatar.png"
    )
    String profilePictureUrl,

    @Schema(
        description = "Accessibility description for the profile picture",
        example = "Portrait of a smiling person wearing a white chef hat"
    )
    String profilePictureAlt
) {
    public UserSummaryDto {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(username, "Username is required");
    }
}

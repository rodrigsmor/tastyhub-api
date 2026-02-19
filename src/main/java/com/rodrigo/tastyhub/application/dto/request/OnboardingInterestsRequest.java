package com.rodrigo.tastyhub.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OnboardingInterestsRequest(
    @Schema(
        description = "List of existing tag IDs to follow",
        example = "[1, 5, 12]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Set<Long> tagIds,

    @Schema(
        description = "Names of new tags to be created and followed",
        example = "[\"homemade pasta\", \"vegan recipes\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Set<String> newTags,

    @Schema(
        description = "List of existing tag IDs to stop following",
        example = "[3, 8]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Set<Long> unfollowTagIds
) {}
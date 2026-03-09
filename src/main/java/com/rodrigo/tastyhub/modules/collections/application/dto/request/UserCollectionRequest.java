package com.rodrigo.tastyhub.modules.collections.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data transfer object for creating or updating a user's recipe collection")
public record UserCollectionRequest(
    @Schema(example = "Summer BBQ", description = "The unique name of the collection")
    @NotBlank
    @Size(min = 3, max = 100)
    String name,

    @Schema(example = "Best recipes for outdoor grilling", description = "A brief description of the collection's purpose")
    @Size(max = 255)
    String description,

    @Schema(example = "false", description = "If true, the collection is pinned/fixed at the top of the list")
    boolean isFixed,

    @Schema(example = "true", description = "If true, the collection is visible to other users")
    boolean isPublic
) {}

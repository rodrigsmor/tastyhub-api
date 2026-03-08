package com.rodrigo.tastyhub.modules.collections.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Detailed response representation of a user's collection")
public record UserCollectionResponseDto(
    @Schema(description = "Unique identifier of the collection", example = "1")
    Long id,

    @Schema(description = "Name of the collection", example = "Sunday Brunch Recipes")
    String name,

    @Schema(description = "Detailed description of the collection's purpose", example = "A collection of my favorite breakfast and brunch ideas.")
    String description,

    @Schema(description = "Full URL of the collection's cover image", example = "https://cdn.tastyhub.com/images/collections/brunch.jpg")
    String coverUrl,

    @Schema(description = "Accessibility alternative text for the cover image", example = "A table filled with pancakes and coffee")
    String coverAlt,

    @Schema(description = "Indicates if this is the user's primary favorites collection", example = "false")
    boolean isFavorite,

    @Schema(description = "Indicates if the collection is pinned/fixed at the top of the list", example = "true")
    boolean isFixed,

    @Schema(description = "Visibility status: true for public, false for private", example = "true")
    boolean isPublic,

    @Schema(description = "Indicates if the collection can be deleted (system-protected collections return false)", example = "true")
    boolean isDeletable,

    @Schema(description = "Total count of all items (recipes + articles) in the collection", example = "15")
    long totalItems,

    @Schema(description = "Count of recipes currently in the collection", example = "10")
    long totalRecipes,

    @Schema(description = "Count of articles currently in the collection", example = "5")
    long totalArticles,

    @Schema(description = "Timestamp when the collection was created")
    OffsetDateTime createdAt,

    @Schema(description = "Timestamp of the last update to the collection metadata or items")
    OffsetDateTime updatedAt,

    UserSummaryDto owner
) {}
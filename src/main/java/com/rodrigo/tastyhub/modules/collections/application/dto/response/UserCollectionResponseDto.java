package com.rodrigo.tastyhub.modules.collections.application.dto.response;

import java.time.OffsetDateTime;

public record UserCollectionResponseDto(
    Long id,
    String name,
    String description,
    String coverUrl,
    String coverAlt,
    boolean isFavorite,
    boolean isFixed,
    boolean isPublic,
    boolean isDeletable,
    long totalItems,
    long totalRecipes,
    long totalArticles,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

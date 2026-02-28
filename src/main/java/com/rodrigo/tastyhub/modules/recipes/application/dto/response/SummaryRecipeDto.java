package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;

public record SummaryRecipeDto(
    long id,
    String title,
    long favoritesCount,
    int minCookTime,
    int maxCookTime,
    long ingredientsCount,
    double averageRating,
    String coverUrl,
    String coverAlt,
    UserSummaryDto user
) {}

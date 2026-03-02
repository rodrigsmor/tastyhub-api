package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary representation of a recipe for listings and search results")
public record SummaryRecipeDto(
    @Schema(description = "Unique identifier of the recipe", example = "101")
    long id,

    @Schema(description = "Title of the recipe", example = "Classic Italian Lasagna")
    String title,

    @Schema(description = "Total number of users who favorited this recipe", example = "1250")
    long favoritesCount,

    @Schema(description = "Minimum estimated cooking time in minutes", example = "30")
    int minCookTime,

    @Schema(description = "Maximum estimated cooking time in minutes", example = "45")
    int maxCookTime,

    @Schema(description = "Total number of ingredients required", example = "12")
    long ingredientsCount,

    @Schema(description = "Average user rating from 0.0 to 5.0", example = "4.8")
    double averageRating,

    @Schema(description = "URL of the recipe's main cover image",
            example = "https://cdn.tastyhub.com/images/recipes/lasagna.jpg")
    String coverUrl,

    @Schema(description = "Alternative text for the cover image (accessibility)",
            example = "A delicious lasagna with melting cheese on top")
    String coverAlt,

    @Schema(description = "Simplified information about the recipe creator")
    UserSummaryDto user
) {}

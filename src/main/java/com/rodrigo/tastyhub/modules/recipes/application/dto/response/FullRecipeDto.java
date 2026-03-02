package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeCategory;
import com.rodrigo.tastyhub.modules.tags.application.dto.response.TagDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record FullRecipeDto(
    Long id,
    String title,
    String description,
    RecipeCategory category,
    Integer cookTimeMin,
    Integer cookTimeMax,
    BigDecimal estimatedCost,
    RecipeCurrencyDto currency,
    String coverUrl,
    String coverAlt,
    List<IngredientDto> ingredients,
    List<PreparationStepDto> preparationSteps,
    List<TagDto> tags,
    @Schema(description = "Total number of users who favorited this recipe", example = "1250")
    long favoritesCount,

    @Schema(description = "Total number of ingredients required", example = "12")
    long ingredientsCount,

    @Schema(description = "Average user rating from 0.0 to 5.0", example = "4.8")
    double averageRating,

    @Schema(description = "Average user rating from 0.0 to 5.0", example = "4.8")
    double reviewsCount,

    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

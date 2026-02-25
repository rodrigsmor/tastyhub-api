package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.tags.application.dto.response.TagDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record FullRecipeDto(
    Long id,
    String title,
    String description,
    Integer cookTimeMin,
    Integer cookTimeMax,
    BigDecimal estimatedCost,
    RecipeCurrencyDto currency,
    String coverUrl,
    String coverAlt,
    List<IngredientDto> ingredients,
    List<PreparationStepDto> preparationSteps,
    List<TagDto> tags,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

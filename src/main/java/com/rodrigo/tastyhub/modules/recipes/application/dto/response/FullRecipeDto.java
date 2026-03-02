package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeCategory;
import com.rodrigo.tastyhub.modules.tags.application.dto.response.TagDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Detailed representation of a recipe, including all instructions, ingredients, and metrics")
public record FullRecipeDto(
    @Schema(description = "Unique identifier of the recipe", example = "202")
    Long id,

    @Schema(description = "Full title of the recipe", example = "Traditional Beef Wellington")
    String title,

    @Schema(description = "Detailed description or story behind the recipe",
            example = "A classic English dish consisting of a prime fillet of beef...")
    String description,

    @Schema(description = "The culinary category this recipe belongs to")
    RecipeCategory category,

    @Schema(description = "Minimum estimated preparation and cooking time in minutes", example = "60")
    Integer cookTimeMin,

    @Schema(description = "Maximum estimated preparation and cooking time in minutes", example = "90")
    Integer cookTimeMax,

    @Schema(description = "Estimated cost to prepare the recipe", example = "150.00")
    BigDecimal estimatedCost,

    @Schema(description = "Currency information used for the estimated cost")
    RecipeCurrencyDto currency,

    @Schema(description = "Full URL of the high-resolution cover image",
            example = "https://cdn.tastyhub.com/images/recipes/beef-wellington.jpg")
    String coverUrl,

    @Schema(description = "Accessibility description for the cover image",
            example = "A cross-section view of a Beef Wellington showing the rare beef and puff pastry")
    String coverAlt,

    @Schema(description = "Complete list of ingredients with their respective quantities and units")
    List<IngredientDto> ingredients,

    @Schema(description = "Ordered list of step-by-step preparation instructions")
    List<PreparationStepDto> preparationSteps,

    @Schema(description = "Associated tags for searching and categorization")
    List<TagDto> tags,

    @Schema(description = "Total number of users who favorited this recipe", example = "1250")
    long favoritesCount,

    @Schema(description = "Total number of unique ingredients required", example = "12")
    long ingredientsCount,

    @Schema(description = "Average user rating from 0.0 to 5.0", example = "4.8")
    double averageRating,

    @Schema(description = "Total number of written reviews submitted by users", example = "342")
    double reviewsCount,

    @Schema(description = "Timestamp when the recipe was first published", example = "2026-03-01T20:00:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "Timestamp of the last modification to the recipe", example = "2026-03-01T21:30:00Z")
    OffsetDateTime updatedAt
) {}

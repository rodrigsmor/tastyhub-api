package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Payload to update a recipe")
public record UpdateRecipeDto(
    @Schema(example = "Spaghetti Carbonara", description = "Recipe title")
    @Size(min = 3, max = 100)
    String title,

    @Schema(example = "A classic Italian pasta dish...", description = "Brief description of the recipe")
    String description,

    @Schema(example = "20", description = "Minimum cooking time in minutes")
    @Positive(message = "Time must be positive")
    Integer cookTimeMin,

    @Schema(example = "30", description = "Maximum cooking time in minutes (optional)")
    @Positive(message = "Time must be positive")
    Integer cookTimeMax,

    @Schema(example = "45.50", description = "Estimated total cost")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal estimatedCost,

    @Schema(example = "1", description = "Currency ID (Required if cost is provided)")
    Short currencyId,

    @Schema(description = "Set of tag IDs to categorize the recipe")
    Set<Long> tagIds,

    @Schema(description = "List of step-by-step instructions")
    @NotEmpty(message = "At least one instruction step is required")
    @Valid
    List<UpdateRecipeIngredientDto> steps,

    @Schema(description = "List of ingredients with quantities")
    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    List<UpdateRecipeIngredientDto> ingredients
) {
    public boolean hasCurrency() {
        return currencyId != null && estimatedCost != null;
    }

    public boolean hasTags() {
        return !tagIds.isEmpty();
    }

    public boolean hasSteps() {
        return steps != null && !steps.isEmpty();
    }
}

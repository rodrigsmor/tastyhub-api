package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Payload to create a new recipe")
public record CreateRecipeDto(
    @Schema(example = "Spaghetti Carbonara", description = "Recipe title")
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    String title,

    @Schema(example = "A classic Italian pasta dish...", description = "Brief description of the recipe")
    @NotBlank(message = "Description is required")
    String description,

    @Schema(example = "MEAL", description = "Recipe Category")
    @NotNull
    RecipeCategory category,

    @Schema(example = "20", description = "Minimum cooking time in minutes")
    @NotNull(message = "Minimum cook time is required")
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
    List<PreparationStepRequestDto> steps,

    @Schema(description = "List of ingredients with quantities")
    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    List<RecipeIngredientRequestDto> ingredients
) {
    @AssertTrue(message = "Both estimatedCost and currencyId must be provided together, or both must be null")
    private boolean isCostMappingValid() {
        if (estimatedCost == null && currencyId == null) return true;
        return (estimatedCost != null && currencyId != null);
    }

    @AssertTrue(message = "cookTimeMax must be greater than or equal to cookTimeMin")
    private boolean isTimeRangeValid() {
        if (cookTimeMax == null) return true;
        return cookTimeMax >= cookTimeMin;
    }

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

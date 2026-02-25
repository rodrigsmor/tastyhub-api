package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import com.rodrigo.tastyhub.modules.recipes.domain.model.IngredientUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RecipeIngredientRequestDto(
    @Schema(example = "500.0", description = "Quantity of the ingredient")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    BigDecimal quantity,

    @Schema(example = "1", description = "The ID of the ingredient (e.g., ID for 'Chicken')")
    @NotNull(message = "Ingredient ID is required")
    Long ingredientId,

    @Schema(example = "GRAMS", description = "Unit of measurement")
    @NotNull(message = "Measurement unit is required")
    IngredientUnitEnum ingredientUnit
) {}

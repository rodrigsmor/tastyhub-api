package com.rodrigo.tastyhub.modules.recipes.domain.model;

import java.math.BigDecimal;

public record RecipeIngredientCommand(
    Long id,
    Long ingredientId,
    BigDecimal quantity,
    IngredientUnitEnum unit
) {}
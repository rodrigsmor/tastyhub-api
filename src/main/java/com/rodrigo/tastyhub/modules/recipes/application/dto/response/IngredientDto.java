package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.modules.recipes.domain.model.IngredientUnitEnum;

import java.math.BigDecimal;

public record IngredientDto(
    Long id,
    String name,
    Long ingredientId,
    BigDecimal quantity,
    IngredientUnitEnum unit
) {}

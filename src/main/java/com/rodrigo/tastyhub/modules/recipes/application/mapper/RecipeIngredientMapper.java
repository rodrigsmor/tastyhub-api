package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.IngredientDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeIngredient;

public final class RecipeIngredientMapper {
    private RecipeIngredientMapper() {}

    public static IngredientDto toIngredientDto(RecipeIngredient ingredient) {
        return new IngredientDto(
            ingredient.getId(),
            ingredient.getIngredient().getName(),
            ingredient.getIngredient().getId(),
            ingredient.getQuantity(),
            ingredient.getUnit()
        );
    }
}

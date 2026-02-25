package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.IngredientDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeIngredient;

public final class RecipeIngredientMapper {
    private RecipeIngredientMapper() {}

    public static IngredientDto toIngredientDto(RecipeIngredient ingredient) {
        return new IngredientDto(
            ingredient.getIngredient().getId(),
            ingredient.getIngredient().getName()
        );
    }
}

package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.RecipeIngredientRequestDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeIngredientDto;
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

    public static RecipeIngredientRequestDto toIngredientRequestDto(RecipeIngredient ingredient) {
        return new RecipeIngredientRequestDto(
            ingredient.getQuantity(),
            ingredient.getIngredient().getId(),
            ingredient.getUnit()
        );
    }

    public static UpdateRecipeIngredientDto toUpdateDto(RecipeIngredient ingredient) {
        return new UpdateRecipeIngredientDto(
            ingredient.getId(),
            ingredient.getQuantity(),
            ingredient.getIngredient().getId(),
            ingredient.getUnit()
        );
    }
}

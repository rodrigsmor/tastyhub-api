package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.tags.application.mapper.TagMapper;

public final class RecipeMapper {
    private RecipeMapper() {}

    public static FullRecipeDto toFullRecipeDto(Recipe recipe) {
        return new FullRecipeDto(
            recipe.getId(),
            recipe.getTitle(),
            recipe.getDescription(),
            recipe.getCookTimeMin(),
            recipe.getCookTimeMax(),
            recipe.getEstimatedCost(),
            CurrencyMapper.toRecipeCurrencyDto(recipe.getCurrency()),
            recipe.getCoverUrl(),
            recipe.getCoverAlt(),
            recipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientDto).toList(),
            recipe.getSteps().stream().map(PreparationStepMapper::toPreparationStepDto).toList(),
            recipe.getTags().stream().map(TagMapper::toTagDto).toList(),
            recipe.getCreatedAt(),
            recipe.getUpdatedAt()
        );
    }
}

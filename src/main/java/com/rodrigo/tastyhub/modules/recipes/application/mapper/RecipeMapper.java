package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.SummaryRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.PreparationStep;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.tags.application.mapper.TagMapper;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public final class RecipeMapper {
    private static ImageStorageService storageService;

    @Autowired
    public void setStorageService(ImageStorageService storageService) {
        RecipeMapper.storageService = storageService;
    }

    public static SummaryRecipeDto toSummaryDto(Recipe recipe) {
        return new SummaryRecipeDto(
            recipe.getId(),
            recipe.getTitle(),
            0,
            recipe.getCookTimeMax() != null ? recipe.getCookTimeMax() : 0,
            recipe.getCookTimeMin() != null ? recipe.getCookTimeMin() : 0,
            0,
            0,
            storageService.generateImageUrl(recipe.getCoverUrl()),
            recipe.getCoverAlt(),
            UserMapper.toSummary(recipe.getAuthor())
        );
    }

    public static FullRecipeDto toFullRecipeDto(Recipe recipe) {
        return new FullRecipeDto(
            recipe.getId(),
            recipe.getTitle(),
            recipe.getDescription(),
            recipe.getCategory(),
            recipe.getCookTimeMin(),
            recipe.getCookTimeMax(),
            recipe.getEstimatedCost(),
            recipe.getCurrency() != null
                ? CurrencyMapper.toRecipeCurrencyDto(recipe.getCurrency())
                : null,
            storageService.generateImageUrl(recipe.getCoverUrl()),
            recipe.getCoverAlt(),
            recipe.getIngredients()
                .stream()
                .map(RecipeIngredientMapper::toIngredientDto)
                .toList(),
            recipe.getSteps()
                .stream()
                .sorted(Comparator.comparing(PreparationStep::getStepNumber))
                .map(PreparationStepMapper::toPreparationStepDto)
                .toList(),
            recipe.getTags().stream().map(TagMapper::toTagDto).toList(),
            0,
            0,
            0,
            0,
            recipe.getCreatedAt(),
            recipe.getUpdatedAt()
        );
    }
}

package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Ingredient;
import com.rodrigo.tastyhub.modules.recipes.domain.model.PreparationStep;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.CurrencyService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.IngredientService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateRecipeUseCase {
    private final TagService tagService;
    private final RecipeService recipeService;
    private final SecurityService securityService;
    private final CurrencyService currencyService;
    private final IngredientService ingredientService;

    @RequiresVerification
    @Transactional
    public FullRecipeDto execute(CreateRecipeDto newData) {
        User author = securityService.getCurrentUser();

        if (!newData.hasSteps()) {
            throw new DomainException("Recipe must have at least one step!");
        }

        Recipe recipe = Recipe
            .builder()
            .title(newData.title())
            .category(newData.category())
            .description(newData.description())
            .cookTimeMin(newData.cookTimeMin())
            .cookTimeMax(newData.cookTimeMax())
            .author(author)
            .build();

        if (newData.hasCurrency()) {
            Currency currency = currencyService.findById(newData.currencyId());
            recipe.updateMonetaryDetails(newData.estimatedCost(), currency);
        }

        if (newData.hasTags()) {
            List<Tag> tags = tagService.syncAll(newData.tagIds());
            recipe.updateAllTags(tags);
        }

        newData.steps().forEach(stepDto -> {
            PreparationStep step = PreparationStep.builder()
                .stepNumber(stepDto.stepNumber())
                .instruction(stepDto.instruction())
                .build();

            recipe.addStep(step);
        });

        newData.ingredients().forEach(ingredientDto -> {
            Optional<Ingredient> ingredient = ingredientService.findById(ingredientDto.ingredientId());
            ingredient.ifPresent(value -> recipe.addIngredient(value, ingredientDto.quantity(), ingredientDto.unit()));
        });

        Recipe savedRecipe = recipeService.create(recipe);

        return RecipeMapper.toFullRecipeDto(savedRecipe);
    }
}

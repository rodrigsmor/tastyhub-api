package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.service.CurrencyService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.IngredientService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new IllegalArgumentException("Recipe must have at least one step!");
        }

        List<PreparationStep> steps = newData.steps()
            .stream()
            .map(step -> new PreparationStep(
                step.stepNumber(),
                step.instruction()
            ))
            .toList();

        List<RecipeIngredient> ingredients = newData.ingredients()
            .stream()
            .map(recipeIngredient -> {
                Ingredient ingredient = ingredientService.findByIdOrThrow(recipeIngredient.ingredientId());
                return new RecipeIngredient(
                    recipeIngredient.quantity(),
                    recipeIngredient.unit(),
                    ingredient
                );
            })
            .toList();

        Recipe recipe = new Recipe(
            newData.title(),
            newData.description(),
            author,
            newData.isPublic(),
            newData.category(),
            newData.cookTimeMin(),
            newData.cookTimeMax(),
            newData.hasCurrency() ? newData.estimatedCost() : null,
            newData.hasCurrency() ? currencyService.findById(newData.currencyId()) : null,
            newData.hasTags() ? tagService.syncAll(newData.tagIds()) : null,
            steps,
            ingredients
        );

        return RecipeMapper.toFullRecipeDto(recipeService.create(recipe));
    }
}

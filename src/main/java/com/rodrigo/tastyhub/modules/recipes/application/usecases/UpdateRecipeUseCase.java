package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeIngredientMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.PreparationStep;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeIngredient;
import com.rodrigo.tastyhub.modules.recipes.domain.service.CurrencyService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.IngredientService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateRecipeUseCase {
    private final TagService tagService;
    private final RecipeService recipeService;
    private final SecurityService securityService;
    private final CurrencyService currencyService;
    private final IngredientService ingredientService;

    @RequiresVerification
    @Transactional
    public FullRecipeDto execute(Long recipeId, UpdateRecipeDto newData) {
        Long userId = securityService.getCurrentUser().getId();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

       recipe.validateOwnership(userId);

        List<RecipeIngredient> ingredients = ingredientService.preparerAll(
            newData.ingredients()
                .stream()
                .map(RecipeIngredientMapper::toCommand)
                .toList(),
            recipe
        );

       recipe.update(
           newData.title(),
           newData.description(),
           newData.category(),
           newData.isPublic(),
           newData.cookTimeMin(),
           newData.cookTimeMax(),
           newData.estimatedCost(),
           newData.currencyId() != null ? currencyService.findById(newData.currencyId()) : null,
           newData.tagIds() != null ? tagService.syncAll(newData.tagIds()) : null,
           ingredients,
           newData.steps().stream().map(step -> new PreparationStep(
               step.id(),
               step.stepNumber(),
               step.instruction(),
               recipe
           )).toList()
       );

        return RecipeMapper.toFullRecipeDto(
            recipeService.update(
                recipe
            )
        );
    }
}

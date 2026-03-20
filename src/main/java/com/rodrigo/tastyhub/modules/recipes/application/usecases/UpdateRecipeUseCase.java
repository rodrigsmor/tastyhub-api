package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.CurrencyService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateRecipeUseCase {
    private final SecurityService securityService;
    private final RecipeService recipeService;
    private final CurrencyService currencyService;
    private final TagService tagService;

    @RequiresVerification
    @Transactional
    public FullRecipeDto execute(Long recipeId, UpdateRecipeDto newData) {
        Long userId = securityService.getCurrentUser().getId();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

       recipe.validateOwnership(userId);

       recipe.update(
           newData.title(),
           newData.description(),
           newData.category(),
           newData.cookTimeMin(),
           newData.cookTimeMax(),
           newData.estimatedCost(),
           newData.currencyId() != null ? currencyService.findById(newData.currencyId()) : null,
           newData.tagIds() != null ? tagService.syncAll(newData.tagIds()) : null
       );

        return RecipeMapper.toFullRecipeDto(
            recipeService.updateAndSync(
                recipe,
                newData.ingredients(),
                newData.steps()
            )
        );
    }
}

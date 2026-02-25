package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.CurrencyRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final SecurityService securityService;
    private final RecipeRepository recipeRepository;
    private final CurrencyService currencyService;

    public Long getRecipesCountByUserId(Long userId) {
        return recipeRepository.countByUserId(userId);
    }

    @RequiresVerification
    @Transactional
    public FullRecipeDto createRecipe(CreateRecipeDto recipeDto) throws BadRequestException {
        User user = this.securityService.getCurrentUser();

        Recipe recipe = Recipe
            .builder()
            .title(recipeDto.title())
            .description(recipeDto.description())
            .cookTimeMin(recipeDto.cookTimeMin())
            .cookTimeMax(recipeDto.cookTimeMax())
            .build();

        if (recipeDto.currencyId() != null) {
            Currency currency = currencyService.findById(recipeDto.currencyId());
            recipe.setCurrency(currency);
            recipe.setEstimatedCost(recipeDto.estimatedCost());
        }

        if (recipeDto.hasTags()) {
//
        }

        Recipe savedRecipe = recipeRepository.save(recipe);

        return RecipeMapper.toFullRecipeDto(savedRecipe);
    }

    // CREATE, READ, UPDATE, DELETE
}

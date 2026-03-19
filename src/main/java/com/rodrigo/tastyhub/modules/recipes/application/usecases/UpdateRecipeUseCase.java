package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.CurrencyService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to update this recipe");
        }

        Optional.ofNullable(newData.title()).ifPresent(recipe::setTitle);
        Optional.ofNullable(newData.description()).ifPresent(recipe::setDescription);

        recipe.updateTiming(recipe.getCookTimeMin(), recipe.getCookTimeMax());

        Currency currency = null;

        if (newData.currencyId() != null) {
            currency = currencyService.findById(newData.currencyId());
        }

        recipe.updateMonetaryDetails(newData.estimatedCost(), currency);

        Optional.ofNullable(newData.tagIds())
            .ifPresent(ids -> {
                List<Tag> tags = tagService.syncAll(ids);
                recipe.updateAllTags(tags);
            });

        return RecipeMapper.toFullRecipeDto(recipeService.update(recipe, newData.ingredients(), newData.steps()));
    }
}

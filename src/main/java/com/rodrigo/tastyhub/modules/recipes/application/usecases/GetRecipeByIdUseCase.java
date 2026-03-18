package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.kernel.application.CanAccessResourceVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetRecipeByIdUseCase {
    private final SecurityService securityService;
    private final RecipeService recipeService;
    private final CanAccessResourceVerification canAccessResource;

    public FullRecipeDto execute(Long recipeId) {
        Optional<User> user = securityService.getCurrentUserOptional();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);
        User recipeOwner = recipe.getAuthor();

        canAccessResource.verify(recipeOwner, user.map(User::getId).orElse(null));

        return RecipeMapper.toFullRecipeDto(recipe);
    }
}

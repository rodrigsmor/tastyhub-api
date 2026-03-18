package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListRecipesUseCase {
    private final SecurityService securityService;
    private final RecipeService recipeService;

    @Transactional
    public RecipePagination execute(ListRecipesQuery query) {
        Optional<User> user = this.securityService.getCurrentUserOptional();

        return this.recipeService.getRecipesList(query, null, null);
    }
}

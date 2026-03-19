package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.service.UserCollectionService;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListRecipesByCollectionUseCase {
    private final RecipeService recipeService;
    private final SecurityService securityService;
    private final UserCollectionService collectionService;

    @Transactional
    public RecipePagination execute(Long collectionId, ListRecipesQuery query) {
        if (collectionId == null) {
            throw new DomainException("Collection ID cannot be null. Please, provide a valid id.");
        }

        Optional<User> user = this.securityService.getCurrentUserOptional();

        UserCollection collection = this.collectionService.findByIdOrThrow(collectionId);

        return this.recipeService.getRecipesList(query, null, collection);
    }
}

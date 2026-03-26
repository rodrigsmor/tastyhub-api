package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.shared.kernel.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteRecipeUseCase {
    private final RecipeService recipeService;
    private final SecurityService securityService;

    @RequiresVerification
    @Transactional
    public void execute(Long recipeId) {
        User user = securityService.getCurrentUser();

        recipeService.deleteById(recipeId, user.getId());
    }
}

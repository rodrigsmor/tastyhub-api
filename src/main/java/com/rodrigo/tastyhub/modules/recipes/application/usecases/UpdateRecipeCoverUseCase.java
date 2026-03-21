package com.rodrigo.tastyhub.modules.recipes.application.usecases;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UpdateRecipeCoverUseCase {
    private final RecipeService recipeService;
    private final SecurityService securityService;
    private final ImageStorageService imageStorageService;

    @RequiresVerification
    @FileCleanup
    @Transactional
    public FullRecipeDto execute(Long recipeId, MultipartFile file, String newAlternativeText) {
        User user = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        String oldFileName = recipe.getCoverUrl();
        String newFileName = imageStorageService.storeImage(file);

        Recipe updatedRecipe = recipeService.updateCoverById(
            recipeId,
            user,
            newFileName,
            newAlternativeText
        );

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return RecipeMapper.toFullRecipeDto(updatedRecipe);
    }
}

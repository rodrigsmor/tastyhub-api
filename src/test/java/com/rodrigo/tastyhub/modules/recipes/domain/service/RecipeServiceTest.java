package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeCategory;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class RecipeServiceTest {
    @Mock
    private TagService tagService;

    @Mock
    private SecurityService securityService;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Nested
    @DisplayName("Tests for getRecipesCountByUserId Method")
    class getRecipesCountByUserId {
        @Test
        @DisplayName("Should return the number of the user's recipes")
        void shouldReturnTheNumberOfAuthorsRecipes() {
            Long userId = 1L;

            when(recipeRepository.countByAuthorId(userId)).thenReturn(10L);

            Long result = recipeService.getRecipesCountByUserId(userId);

            assertEquals(10L, result);
            verify(recipeRepository, times(1)).countByAuthorId(eq(userId));
        }
    }

    @Nested
    @DisplayName("Tests for FindByIdOrThrow Method")
    class FindByIdOrThrowTests {
        @Test
        @DisplayName("Should return the Recipe corresponding to the specified ID")
        void shouldReturnTheRecipeCorrespondingToSpecifiedId() {
            Long recipeId = 1L;

            Recipe recipe = Recipe
                .builder()
                .title("Recipe mock")
                .category(RecipeCategory.SNACK)
                .description("Lorem ipsum")
                .cookTimeMin(10)
                .cookTimeMax(60)
                .author(new User())
                .build();

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

            Recipe response = recipeService.findByIdOrThrow(recipeId);

            assertEquals(recipe, response);
            verify(recipeRepository, times(1)).findById(eq(recipeId));
        }

        @Test
        @DisplayName("Should throws domain exception when recipe is not found")
        void shouldThrowsResourceNotFoundWhenRecipeIsNotFound() {
            Long recipeId = 1L;

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> recipeService.findByIdOrThrow(recipeId));
        }
    }
}
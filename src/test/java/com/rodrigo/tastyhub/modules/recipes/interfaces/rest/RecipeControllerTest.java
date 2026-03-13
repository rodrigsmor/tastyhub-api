package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    private Recipe fakeRecipe;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        fakeRecipe = new Recipe();
        fakeRecipe.setId(1L);
        fakeRecipe.setTitle("Fake Recipe");
        fakeRecipe.setDescription("waved about helplessly as he looked. What's happened to me? he thought.");
        fakeRecipe.setCookTimeMin(0);
        fakeRecipe.setCookTimeMax(10);
        fakeRecipe.setAuthor(new User());
        fakeRecipe.setCoverUrl("http://example.com");
        fakeRecipe.setCoverAlt("Alternative");
        fakeRecipe.setEstimatedCost(new BigDecimal("10.5"));

        RecipeStatistics recipeStatistics = new RecipeStatistics();

        recipeStatistics.incrementRating(new BigDecimal("4.5"));
        recipeStatistics.setFavoritesCount(0);

        fakeRecipe.setStatistics(recipeStatistics);

        PreparationStep firstStep = PreparationStep.builder().id(1L).stepNumber(0).instruction("First Step").build();
        PreparationStep secondStep = PreparationStep.builder().id(2L).stepNumber(1).instruction("Second Step").build();
        PreparationStep thirdStep = PreparationStep.builder().id(3L).stepNumber(2).instruction("Third Step").build();

        List<PreparationStep> steps = new ArrayList<>(List.of(firstStep, secondStep, thirdStep));

        fakeRecipe.setSteps(steps);

        Currency currency = new Currency(
            (short) 1,
            "USD",
            "American Dollar",
            "$"
        );

        fakeRecipe.setCurrency(currency);
        fakeRecipe.setIngredients(new ArrayList<>());
        fakeRecipe.setMedia(new ArrayList<>());
        fakeRecipe.setComments(new ArrayList<>());
        fakeRecipe.setTags(new HashSet<>());
        fakeRecipe.setCategory(RecipeCategory.SNACK);
        fakeRecipe.setCreatedAt(OffsetDateTime.now());
        fakeRecipe.setUpdatedAt(OffsetDateTime.now());
    }

    private static final String BASE_URL = "/api/recipes";

    @Nested
    @DisplayName("GET /api/recipes/{id}")
    class GetRecipeByIdTests {
        @Test
        @DisplayName("1. Should return 200 and the recipe details when ID exists")
        void shouldReturn200WhenRecipeExists() throws Exception {
            Long recipeId = 1L;
            Recipe mockRecipe = fakeRecipe;

            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            mockMvc.perform(get(BASE_URL + "/{id}", recipeId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipeId))
                .andExpect(jsonPath("$.title").value("Fake Recipe"));
        }

        @Test
        @DisplayName("2. Should return 404 when recipe ID does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            Long recipeId = 999L;
            when(recipeService.findByIdOrThrow(recipeId))
                .thenThrow(new ResourceNotFoundException("Recipe not found with the provided ID"));

            mockMvc.perform(get(BASE_URL + "/{id}", recipeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recipe not found with the provided ID"))
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("3. Should return 400 when ID is less than 1 (Validation)")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get(BASE_URL + "/{id}", invalidId))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(recipeService);
        }

        @Test
        @DisplayName("4. Should return 500 when an unexpected error occurs")
        void shouldReturn500OnUnexpectedError() throws Exception {
            Long recipeId = 1L;
            when(recipeService.findByIdOrThrow(recipeId))
                .thenThrow(new RuntimeException("Database connection failure"));

            mockMvc.perform(get(BASE_URL + "/{id}", recipeId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
        }
    }
}
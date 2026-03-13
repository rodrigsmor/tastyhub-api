package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.PreparationStepMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeIngredientMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RecipeController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        User author = User
            .builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .username("chef_johndoe")
            .profilePictureUrl(null)
            .profilePictureAlt(null)
            .build();

        fakeRecipe.setAuthor(author);
        fakeRecipe.setCoverUrl("http://example.com");
        fakeRecipe.setCoverAlt("Alternative");
        fakeRecipe.setEstimatedCost(new BigDecimal("10.5"));

        RecipeStatistics recipeStatistics = new RecipeStatistics();

        recipeStatistics.incrementRating(new BigDecimal("4.5"));
        recipeStatistics.setFavoritesCount(0);

        fakeRecipe.setStatistics(recipeStatistics);

        PreparationStep firstStep = PreparationStep.builder().id(1L).stepNumber(1).instruction("First Step").build();
        PreparationStep secondStep = PreparationStep.builder().id(2L).stepNumber(2).instruction("Second Step").build();
        PreparationStep thirdStep = PreparationStep.builder().id(3L).stepNumber(3).instruction("Third Step").build();

        List<PreparationStep> steps = new ArrayList<>(List.of(firstStep, secondStep, thirdStep));

        fakeRecipe.setSteps(steps);

        Currency currency = new Currency(
            (short) 1,
            "USD",
            "American Dollar",
            "$"
        );

        fakeRecipe.setCurrency(currency);

        RecipeIngredient ingredient = RecipeIngredient
            .builder()
            .quantity(new BigDecimal("10.5"))
            .unit(IngredientUnitEnum.GRAM)
            .ingredient(new Ingredient(1L, "Ingrediente"))
            .build();

        fakeRecipe.setIngredients(new ArrayList<>(List.of(ingredient)));
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

    @Nested
    @DisplayName("GET /api/recipes (Listing and Filtering)")
    class ListRecipesTests {
        @Test
        @DisplayName("1. Should return 200 and paginated list with default parameters")
        void shouldReturnPaginatedRecipes() throws Exception {
            RecipePagination mockPagination = new RecipePagination(
                List.of(RecipeMapper.toSummaryDto(fakeRecipe)),
                new PaginationMetadata(
                    1,
                    10,
                    1,
                    1L,
                    SortDirection.ASC,
                    false,
                    false
                )
            );

            when(recipeService.listRecipes(any(ListRecipesQuery.class))).thenReturn(mockPagination);

            mockMvc.perform(get("/api/recipes")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipes").isArray())
                .andExpect(jsonPath("$.metadata.page").value(1))
                .andExpect(jsonPath("$.metadata.totalItems").value(1));
        }

        @Test
        @DisplayName("2. Should apply filters correctly and return 200")
        void shouldApplyFiltersCorrectly() throws Exception {
            when(recipeService.listRecipes(any(ListRecipesQuery.class)))
                .thenReturn(new RecipePagination(
                    List.of(),
                    new PaginationMetadata(
                        1,
                        10,
                        1,
                        1L,
                        SortDirection.ASC,
                        false,
                        false
                    )
                ));

            mockMvc.perform(get("/api/recipes")
                    .param("tags", "vegan", "pasta")
                    .param("minRating", "4")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            verify(recipeService).listRecipes(argThat(query ->
                query.tags().containsAll(List.of("vegan", "pasta")) &&
                query.minRating() == 4 &&
                query.size() == 20
            ));
        }

        @Test
        @DisplayName("3. Should return 400 when validation in ListRecipesQuery fails")
        void shouldReturn400OnInvalidParams() throws Exception {
            mockMvc.perform(get("/api/recipes")
                    .param("size", "-1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/recipes (Create Recipe)")
    class CreateRecipeTests {
        @Test
        @DisplayName("1. Should return 201 and Location header when recipe is created")
        void shouldReturn201WhenCreated() throws Exception {
            CreateRecipeDto requestDto = new CreateRecipeDto(
                "Beef Wellington",
                "Classic dish",
                RecipeCategory.SNACK,
                60,
                90,
                new BigDecimal("150.00"),
                (short) 2,
                null,
                fakeRecipe.getSteps().stream().map(PreparationStepMapper::toStepRequestDto).toList(),
                fakeRecipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientRequestDto).toList()
            );

            FullRecipeDto mockResponse = new FullRecipeDto(
                101L,
                "Beef Wellington",
                "Classic dish",
                RecipeCategory.SNACK,
                60,
                90,
                new BigDecimal("150.00"),
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                0,
                0,
                0.0,
                0.0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );

            when(recipeService.createRecipe(any(CreateRecipeDto.class))).thenReturn(mockResponse);

            mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/api/recipes/101"))) // Valida o URI
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.title").value("Beef Wellington"));
        }

        @Test
        @DisplayName("2. Should return 400 when required fields are missing (@Valid)")
        void shouldReturn400OnInvalidInput() throws Exception {
            CreateRecipeDto invalidDto = new CreateRecipeDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(recipeService);
        }

        @Test
        @DisplayName("3. Should return 403 when user is not verified")
        void shouldReturn403WhenUnauthorized() throws Exception {
            CreateRecipeDto requestDto = new CreateRecipeDto(
                "Title",
                "Desc",
                RecipeCategory.SOUP,
                10,
                20,
                null,
                null,
                null,
                fakeRecipe.getSteps().stream().map(PreparationStepMapper::toStepRequestDto).toList(),
                fakeRecipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientRequestDto).toList()
            );

            when(recipeService.createRecipe(any()))
                .thenThrow(new ForbiddenException("User must be verified to create recipes"));

            mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User must be verified to create recipes"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/recipes/{id}")
    class DeleteRecipeTests {
        @Test
        @DisplayName("1. Should return 204 when recipe is deleted successfully")
        void shouldReturn204WhenDeleted() throws Exception {
            Long recipeId = 1L;

            doNothing().when(recipeService).deleteRecipeById(recipeId);

            mockMvc.perform(delete("/api/recipes/{id}", recipeId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            verify(recipeService, times(1)).deleteRecipeById(recipeId);
        }

        @Test
        @DisplayName("2. Should return 404 when recipe to delete does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            Long recipeId = 999L;
            doThrow(new ResourceNotFoundException("Recipe not found"))
                .when(recipeService).deleteRecipeById(recipeId);

            mockMvc.perform(delete("/api/recipes/{id}", recipeId))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("3. Should return 403 when user is not the owner or admin")
        void shouldReturn403WhenForbidden() throws Exception {
            Long recipeId = 1L;
            doThrow(new ForbiddenException("You do not have permission to delete this recipe"))
                .when(recipeService).deleteRecipeById(recipeId);

            mockMvc.perform(delete("/api/recipes/{id}", recipeId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to delete this recipe"));
        }
    }
}
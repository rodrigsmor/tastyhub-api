package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.PreparationStepMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeIngredientMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe fakeRecipe;

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

    @Nested
    @DisplayName("Tests for Create Recipe Method")
    class CreateRecipe {
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

        @Test
        @DisplayName("Should throw a domain exception when the recipe has no steps")
        void shouldThrowDomainExceptionWhenRecipeHasNoSteps() {
            CreateRecipeDto mockRecipeDto = new CreateRecipeDto(
                fakeRecipe.getTitle(),
                fakeRecipe.getDescription(),
                fakeRecipe.getCategory(),
                fakeRecipe.getCookTimeMin(),
                fakeRecipe.getCookTimeMax(),
                fakeRecipe.getEstimatedCost(),
                fakeRecipe.getCurrency().getId(),
                fakeRecipe.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),
//                fakeRecipe.getSteps().stream().map(PreparationStepMapper::toStepRequestDto).toList(),
                null,
                fakeRecipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientRequestDto).toList()
            );

            assertThrows(DomainException.class, () -> recipeService.createRecipe(mockRecipeDto));

            verify(currencyService, never()).findById(any());
            verify(securityService, never()).getCurrentUser();
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create and return recipe successfully")
        void shouldCreateAndReturnRecipeSuccessfully() {
            CreateRecipeDto mockRecipeDto = new CreateRecipeDto(
                fakeRecipe.getTitle(),
                fakeRecipe.getDescription(),
                fakeRecipe.getCategory(),
                fakeRecipe.getCookTimeMin(),
                fakeRecipe.getCookTimeMax(),
                null,
                null,
                fakeRecipe.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),
                fakeRecipe.getSteps().stream().map(PreparationStepMapper::toStepRequestDto).toList(),
                fakeRecipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientRequestDto).toList()
            );

            Recipe mockRecipe = Recipe
                .builder()
                .id(1L)
                .title(mockRecipeDto.title())
                .category(mockRecipeDto.category())
                .description(mockRecipeDto.description())
                .cookTimeMin(mockRecipeDto.cookTimeMin())
                .cookTimeMax(mockRecipeDto.cookTimeMax())
                .author(fakeRecipe.getAuthor())
                .steps(fakeRecipe.getSteps())
                .estimatedCost(null)
                .statistics(fakeRecipe.getStatistics())
                .build();

            when(recipeRepository.save(any())).thenReturn(mockRecipe);

            FullRecipeDto results = recipeService.createRecipe(mockRecipeDto);

            assertEquals(RecipeMapper.toFullRecipeDto(mockRecipe), results);

            verify(securityService, times(1)).getCurrentUser();
            verify(recipeRepository, times(1)).save(any(Recipe.class));
        }

        @Test
        @DisplayName("Should update monetary details when currency is present")
        void shouldUpdateMonetaryDetailsWhenCurrencyIsPresent() {
            CreateRecipeDto mockRecipeDto = new CreateRecipeDto(
                fakeRecipe.getTitle(),
                fakeRecipe.getDescription(),
                fakeRecipe.getCategory(),
                fakeRecipe.getCookTimeMin(),
                fakeRecipe.getCookTimeMax(),
                fakeRecipe.getEstimatedCost(),
                fakeRecipe.getCurrency().getId(),
                fakeRecipe.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),
                fakeRecipe.getSteps().stream().map(PreparationStepMapper::toStepRequestDto).toList(),
                fakeRecipe.getIngredients().stream().map(RecipeIngredientMapper::toIngredientRequestDto).toList()
            );

            Currency currency = fakeRecipe.getCurrency();
            ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);

            when(currencyService.findById(mockRecipeDto.currencyId())).thenReturn(currency);
            when(recipeRepository.save(any())).thenReturn(fakeRecipe);

            recipeService.createRecipe(mockRecipeDto);

            verify(currencyService, times(1)).findById(mockRecipeDto.currencyId());

            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe capturedRecipe = recipeCaptor.getValue();

            assertEquals(mockRecipeDto.estimatedCost(), capturedRecipe.getEstimatedCost());
            assertEquals(currency, capturedRecipe.getCurrency());
        }
    }
}
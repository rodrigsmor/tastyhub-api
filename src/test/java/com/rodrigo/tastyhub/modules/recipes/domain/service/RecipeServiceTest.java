package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.*;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.PreparationStepMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeIngredientMapper;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
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

    @Mock
    private ImageStorageService imageStorageService;

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

    @Nested
    @DisplayName("Tests for List Recipes Method")
    class ListRecipes {
        @Test
        @DisplayName("Should return paginated recipes successfully")
        void shouldReturnPaginatedRecipesSuccessfully() {
            ListRecipesQuery query = new ListRecipesQuery(
                "search",
                0,
                10,
                RecipeSortBy.CREATION_DATE,
                SortDirection.DESC,
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

            Recipe fakeRecipe = createFakeRecipe();
            Page<Recipe> recipePage = new PageImpl<>(List.of(fakeRecipe), PageRequest.of(0, 10), 1);

            when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(recipePage);

            RecipePagination result = recipeService.listRecipes(query);

            assertNotNull(result);
            assertEquals(1, result.recipes().size());
            assertEquals(0, result.metadata().page());
            assertEquals(1, result.metadata().totalPages());
            assertEquals(1L, result.metadata().totalItems());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(recipeRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(10, capturedPageable.getPageSize());
            assertTrue(capturedPageable.getSort().getOrderFor("createdAt").isDescending());
        }

        @Test
        @DisplayName("Should return empty pagination when no recipes match filters")
        void shouldReturnEmptyPaginationWhenNoRecipesFound() {
            ListRecipesQuery query = new ListRecipesQuery(
                "",
                1,
                10,
                RecipeSortBy.CREATION_DATE,
                SortDirection.DESC,
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
            Page<Recipe> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

            RecipePagination result = recipeService.listRecipes(query);

            assertTrue(result.recipes().isEmpty());
            assertEquals(0, result.metadata().totalItems());
            verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Tests for Delete Recipe Method")
    class DeleteRecipeTests {

        @Test
        @DisplayName("Should delete recipe successfully when user is the author")
        void shouldDeleteRecipeSuccessfully() {
            Long recipeId = 1L;
            Long userId = 10L;

            User author = new User();
            author.setId(userId);

            Recipe recipe = new Recipe();
            recipe.setId(recipeId);
            recipe.setAuthor(author);

            User currentUser = new User();
            currentUser.setId(userId);

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(securityService.getCurrentUser()).thenReturn(currentUser);

            recipeService.deleteRecipeById(recipeId);

            verify(recipeRepository, times(1)).delete(recipe);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when recipe does not exist")
        void shouldThrowResourceNotFoundExceptionWhenRecipeDoesNotExist() {
            Long recipeId = 1L;
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                recipeService.deleteRecipeById(recipeId)
            );

            verify(securityService, never()).getCurrentUser();
            verify(recipeRepository, never()).delete(any(Recipe.class));
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not the author")
        void shouldThrowForbiddenExceptionWhenUserIsNotAuthor() {
            Long recipeId = 1L;

            User author = new User();
            author.setId(10L);

            Recipe recipe = new Recipe();
            recipe.setAuthor(author);

            User stranger = new User();
            stranger.setId(99L);

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(securityService.getCurrentUser()).thenReturn(stranger);

            assertThrows(ForbiddenException.class, () ->
                recipeService.deleteRecipeById(recipeId)
            );

            verify(recipeRepository, never()).delete(any(Recipe.class));
        }
    }

    @Nested
    @DisplayName("Tests for Update Recipe Method")
    class UpdateRecipeTests {
        @Test
        @DisplayName("Should update recipe successfully when user is the author")
        void shouldUpdateRecipeSuccessfully() {
            Long recipeId = 1L;
            Long userId = 10L;

            User author = new User();
            author.setId(userId);

            Recipe recipe = spy(new Recipe());
            recipe.setId(recipeId);
            recipe.setAuthor(author);
            recipe.setCookTimeMin(10);
            recipe.setCookTimeMax(20);

            RecipeStatistics recipeStatistics = new RecipeStatistics();

            recipeStatistics.incrementRating(new BigDecimal("4.5"));
            recipeStatistics.setFavoritesCount(0);

            recipe.setStatistics(recipeStatistics);

            UpdateRecipeDto updateDto = new UpdateRecipeDto(
                "New Title",
                "New Description",
                null,
                15,
                new BigDecimal("50.0"),
                (short) 1,
                Set.of(1L, 2L),
                null,
                null
            );

            User currentUser = new User();
            currentUser.setId(userId);

            Currency mockCurrency = new Currency((short) 1, "USD", "Dollar", "$");

            when(tagService.findAllById(Set.of(1L, 2L))).thenReturn(new ArrayList<>(List.of(
                new Tag(1L, "tag1"),
                new Tag(2L, "tag2")
            )));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(currencyService.findById(any())).thenReturn(mockCurrency);
            when(recipeRepository.save(any())).thenReturn(recipe);

            FullRecipeDto result = recipeService.updateRecipeById(recipeId, updateDto);

            assertNotNull(result);
            assertEquals("New Title", recipe.getTitle());
            assertEquals("New Description", recipe.getDescription());

            verify(recipe).updateTiming(anyInt(), anyInt());
            verify(recipe).updateMonetaryDetails(eq(updateDto.estimatedCost()), eq(mockCurrency));
            verify(recipeRepository).save(recipe);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not the author")
        void shouldThrowForbiddenExceptionWhenUserIsNotAuthor() {
            Long recipeId = 1L;
            User author = new User();
            author.setId(10L);

            Recipe recipe = new Recipe();
            recipe.setAuthor(author);

            User stranger = new User();
            stranger.setId(99L);

            UpdateRecipeDto updateDto = createEmptyUpdateDto();

            when(securityService.getCurrentUser()).thenReturn(stranger);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

            assertThrows(ForbiddenException.class, () ->
                recipeService.updateRecipeById(recipeId, updateDto)
            );

            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should NOT update fields when DTO values are null (Partial Update)")
        void shouldNotUpdateFieldsWhenDtoValuesAreNull() {
            Long recipeId = 1L;
            String originalTitle = "Original Title";

            User author = new User();
            author.setId(1L);

            Recipe recipe = new Recipe();
            recipe.setAuthor(author);
            recipe.setTitle(originalTitle);

            RecipeStatistics recipeStatistics = new RecipeStatistics();

            recipeStatistics.incrementRating(new BigDecimal("4.5"));
            recipeStatistics.setFavoritesCount(0);

            recipe.setStatistics(recipeStatistics);

            UpdateRecipeDto updateDto = new UpdateRecipeDto(
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

            when(securityService.getCurrentUser()).thenReturn(author);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(recipeRepository.save(any())).thenReturn(recipe);

            recipeService.updateRecipeById(recipeId, updateDto);

            assertEquals(originalTitle, recipe.getTitle(), "Title should remain unchanged");
            verify(currencyService, never()).findById(any());
        }

        @Test
        @DisplayName("Should sync tags, steps and ingredients when present in DTO")
        void shouldSyncCollectionsWhenPresentInDto() {
            Long recipeId = 1L;
            User author = new User();
            author.setId(1L);
            Recipe recipe = new Recipe();
            recipe.setAuthor(author);

            RecipeStatistics recipeStatistics = new RecipeStatistics();

            recipeStatistics.incrementRating(new BigDecimal("4.5"));
            recipeStatistics.setFavoritesCount(0);

            recipe.setStatistics(recipeStatistics);

            List<UpdatePreparationStepDto> stepsMock = new ArrayList<>(
                List.of(
                    new UpdatePreparationStepDto(
                        1L,
                        0,
                        "Step 1"
                    )
                )
            );

            List<UpdateRecipeIngredientDto> ingredientsMock = new ArrayList<>(List.of(
                new UpdateRecipeIngredientDto(
                    1L,
                    new BigDecimal("10"),
                    10L,
                    IngredientUnitEnum.GRAM
                )
            ));

            UpdateRecipeDto updateDto = new UpdateRecipeDto(
                "Title",
                "Desc",
                10,
                10,
                null,
                null,
                Set.of(1L),
                stepsMock,
                ingredientsMock
            );

            when(tagService.findAllById(Set.of(1L))).thenReturn(new ArrayList<>(List.of(
                new Tag(1L, "tag1")
            )));
            when(securityService.getCurrentUser()).thenReturn(author);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(recipeRepository.save(any())).thenReturn(recipe);

            recipeService.updateRecipeById(recipeId, updateDto);

            verify(recipeRepository).save(any(Recipe.class));
        }

        private UpdateRecipeDto createEmptyUpdateDto() {
            return new UpdateRecipeDto(null, null, null, null, null, null, null, null, null);
        }
    }

    @Nested
    @DisplayName("Tests for Update Cover Method")
    class UpdateCoverTests {

        @Test
        @DisplayName("Should update cover successfully and delete old image when author is the requester")
        void shouldUpdateCoverSuccessfully() {
            Long recipeId = 1L;
            Long userId = 10L;
            String oldCover = "old-image.jpg";
            String newCover = "new-image.jpg";
            String altText = "New Alt Text";

            User author = new User();
            author.setId(userId);

            Recipe recipe = new Recipe();
            recipe.setId(recipeId);
            recipe.setAuthor(author);
            recipe.setCoverUrl(oldCover);

            User currentUser = new User();
            currentUser.setId(userId);

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(imageStorageService.storeImage(file)).thenReturn(newCover);
            when(recipeRepository.save(any())).thenReturn(recipe);

            Recipe result = recipeService.updateCoverById(recipeId, file, altText);

            assertEquals(newCover, result.getCoverUrl());
            assertEquals(altText, result.getCoverAlt());

            verify(imageStorageService).storeImage(file);
            verify(recipeRepository).save(recipe);
            verify(imageStorageService).deleteImage(oldCover);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not the author")
        void shouldThrowForbiddenExceptionWhenUserIsNotAuthor() {
            Long recipeId = 1L;
            User author = new User();
            author.setId(1L);

            Recipe recipe = new Recipe();
            recipe.setAuthor(author);

            User stranger = new User();
            stranger.setId(99L);

            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            when(securityService.getCurrentUser()).thenReturn(stranger);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

            assertThrows(ForbiddenException.class, () ->
                recipeService.updateCoverById(recipeId, file, "alt")
            );

            verify(imageStorageService, never()).storeImage(any());
            verify(recipeRepository, never()).save(any());
            verify(imageStorageService, never()).deleteImage(any());
        }

        @Test
        @DisplayName("Should update cover but not call deleteImage if oldFileName was null")
        void shouldUpdateCoverButNotDeleteIfOldFileIsNull() {
            Long recipeId = 1L;
            User author = new User();
            author.setId(1L);

            Recipe recipe = new Recipe();
            recipe.setAuthor(author);
            recipe.setCoverUrl(null);

            String newCover = "brand-new.jpg";
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            when(securityService.getCurrentUser()).thenReturn(author);
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(imageStorageService.storeImage(file)).thenReturn(newCover);

            recipeService.updateCoverById(recipeId, file, "alt");

            verify(imageStorageService, never()).deleteImage(anyString());
            verify(recipeRepository).save(recipe);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when recipe does not exist")
        void shouldThrowResourceNotFoundExceptionWhenRecipeMissing() {
            Long recipeId = 1L;
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            when(securityService.getCurrentUser()).thenReturn(new User());
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                recipeService.updateCoverById(recipeId, file, "alt")
            );

            verifyNoInteractions(imageStorageService);
        }
    }

    private Recipe createFakeRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Test Recipe");
        recipe.setAuthor(new User());
        recipe.setStatistics(new RecipeStatistics());
        recipe.setCreatedAt(OffsetDateTime.now());

        User user = User
            .builder()
            .id(1L)
            .firstName("Mock")
            .lastName("Last Name")
            .username("usernamemock")
            .profilePictureUrl("/profile")
            .profilePictureAlt("alt profile")
            .bio("lorem ipsum dolor sit amet")
            .coverPhotoUrl("/cover")
            .coverPhotoAlt("alt cover")
            .build();

        recipe.setAuthor(user);
        return recipe;
    }
}
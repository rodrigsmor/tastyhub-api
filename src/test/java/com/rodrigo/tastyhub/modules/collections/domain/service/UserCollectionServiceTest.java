package com.rodrigo.tastyhub.modules.collections.domain.service;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.request.UserCollectionRequest;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.UserCollectionResponseDto;
import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionSortBy;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.repository.UserCollectionRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCollectionServiceTest {
    @Mock
    private UserCollectionRepository collectionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private SecurityService securityService;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private UserCollectionService collectionService;

    private UserCollection fakeCollection;

    @BeforeEach
    void setup() {
        User author = User
            .builder()
            .id(10L)
            .firstName("John")
            .lastName("Doe")
            .email("johndoe@example.com")
            .username("chef_johndoe")
            .profilePictureUrl("http://cdn.johndoe.com/profile-url")
            .profilePictureAlt("alternative")
            .build();

        this.fakeCollection = new UserCollection();

        fakeCollection.setId(100L);
        fakeCollection.setName("Collection Name");
        fakeCollection.setDescription("Lorem Ipsum dolor sit amet.");
        fakeCollection.setCoverUrl("/cover-collection.url");
        fakeCollection.setCoverAlt("/alternative-text");
        fakeCollection.setFixed(false);
        fakeCollection.setPublic(true);
        fakeCollection.setDeletable(true);
        fakeCollection.setFavorite(false);

        fakeCollection.setUser(author);
    }

    @Nested
    @DisplayName("listCollectionsByUserId Tests")
    class ListCollectionsByUserIdTests {

        private Long userId;
        private ListCollectionQuery queries;

        @BeforeEach
        void setUp() {
            userId = 1L;
            queries = new ListCollectionQuery(
                "",
                0,
                10,
                CollectionSortBy.NAME,
                SortDirection.ASC,
                null
            );
        }

        @Test
        @DisplayName("1. Should return CollectionPagination when user exists and has collections")
        void shouldReturnPaginationSuccessfully() {
            Page<UserCollection> mockPage = new PageImpl<>(
                List.of(fakeCollection),
                PageRequest.of(0, 10),
                1
            );

            when(userService.existsById(userId)).thenReturn(true);
            when(securityService.getCurrentUserOptional()).thenReturn(Optional.empty()); // Visitante deslogado
            when(collectionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

            CollectionPagination result = collectionService.listCollectionsByUserId(userId, queries);

            assertNotNull(result);
            assertEquals(1, result.metadata().totalItems());
            assertEquals(100L, result.collections().get(0).id());

            verify(userService).existsById(userId);
            verify(collectionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("2. Should call repository with currentUserId when user is logged in")
        void shouldInjectCurrentUserIdInFilters() {
            // GIVEN
            Long loggedUserId = 5L;
            User loggedUser = new User();
            loggedUser.setId(loggedUserId);

            when(userService.existsById(userId)).thenReturn(true);
            when(securityService.getCurrentUserOptional()).thenReturn(Optional.of(loggedUser));
            when(collectionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            collectionService.listCollectionsByUserId(userId, queries);

            verify(securityService).getCurrentUserOptional();
            verify(collectionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("3. Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userService.existsById(userId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () ->
                collectionService.listCollectionsByUserId(userId, queries)
            );

            verifyNoInteractions(collectionRepository);
            verifyNoInteractions(securityService);
        }

        @Test
        @DisplayName("4. Should map metadata correctly for multiple pages")
        @SuppressWarnings("unchecked")
        void shouldMapMetadataForMultiplePages() {
            Page<UserCollection> mockPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 5),
                15
            );

            when(userService.existsById(userId)).thenReturn(true);
            when(securityService.getCurrentUserOptional()).thenReturn(Optional.empty());
            when(collectionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

            CollectionPagination result = collectionService.listCollectionsByUserId(userId,
                new ListCollectionQuery(
                    "",
                    0,
                    5,
                    CollectionSortBy.NAME,
                    SortDirection.DESC,
                    null
                )
            );

            assertEquals(3, result.metadata().totalPages());
            assertTrue(result.metadata().hasNext());
            assertFalse(result.metadata().hasPrevious());
        }
    }

    @Nested
    @DisplayName("createCollection Tests")
    class CreateCollectionTests {
        @Test
        @DisplayName("1. Should create collection with cover image successfully")
        void shouldCreateCollectionWithImage() {
            UserCollectionRequest request = new UserCollectionRequest(
                "Desserts",
                "My favs",
                true,
                true
            );

            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
            );
            String altText = "Cover alt text";

            User mockUser = User
                .builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("johdoe@example.com")
                .username("chef_johndoe")
                .profilePictureUrl("http://cdn.johndoe.com/profile-url")
                .profilePictureAlt("alternative")
                .build();

            when(securityService.getCurrentUserOptional()).thenReturn(Optional.of(mockUser));
            when(imageStorageService.storeImage(any())).thenReturn("stored_filename.jpg");
            when(collectionRepository.save(any(UserCollection.class))).thenAnswer(i -> {
                UserCollection c = i.getArgument(0);
                c.setId(500L);
                return c;
            });

            UserCollectionResponseDto result = collectionService.createCollection(request, file, altText);

            assertNotNull(result);
            verify(imageStorageService, times(1)).storeImage(file);
            verify(collectionRepository).save(argThat(c ->
                c.getName().equals("Desserts") &&
                c.getCoverUrl().equals("stored_filename.jpg") &&
                c.getUser().equals(mockUser)
            ));
        }

        @Test
        @DisplayName("2. Should create collection without image when file is null")
        void shouldCreateCollectionWithoutImage() {
            User mockUser = User
                .builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("johdoe@example.com")
                .username("chef_johndoe")
                .profilePictureUrl("http://cdn.johndoe.com/profile-url")
                .profilePictureAlt("alternative")
                .build();

            UserCollectionRequest request = new UserCollectionRequest(
                "No Image",
                "Desc",
                false,
                true
            );
            when(securityService.getCurrentUserOptional()).thenReturn(Optional.of(mockUser));
            when(collectionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            collectionService.createCollection(request, null, null);

            verifyNoInteractions(imageStorageService);
            verify(collectionRepository).save(argThat(c -> c.getCoverUrl() == null));
        }

        @Test
        @DisplayName("3. Should throw UnauthorizedException when user is not logged in")
        void shouldThrowUnauthorizedWhenNoUser() {
            when(securityService.getCurrentUserOptional()).thenReturn(Optional.empty());

            assertThrows(UnauthorizedException.class, () ->
                collectionService.createCollection(
                    new UserCollectionRequest(
                        "X",
                        "Y",
                        true,
                        true
                    ),
                    null,
                    null
                )
            );

            verifyNoInteractions(collectionRepository);
            verifyNoInteractions(imageStorageService);
        }
    }

    @Nested
    @DisplayName("favoriteRecipe Tests")
    class FavoriteRecipeTests {
        @Test
        @DisplayName("1. Should add recipe to favorites successfully")
        void shouldAddRecipeToFavorites() {
            Long recipeId = 1L;
            User mockUser = spy(new User());
            Recipe mockRecipe = new Recipe();
            mockRecipe.setId(recipeId);

            UserCollection favorites = new UserCollection();
            favorites.setName("Favorites");
            favorites.setRecipes(new HashSet<>());

            when(securityService.getCurrentUser()).thenReturn(mockUser);

            doReturn(favorites).when(mockUser).getFavoritesCollection();

            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            collectionService.favoriteRecipe(recipeId);

            assertTrue(favorites.getRecipes().contains(mockRecipe));
            verify(collectionRepository, times(1)).saveAndFlush(favorites);
        }

        @Test
        @DisplayName("2. Should throw DomainException when recipe is already favorited")
        void shouldThrowExceptionWhenAlreadyFavorited() {
            Long recipeId = 1L;
            User mockUser = spy(new User());
            Recipe mockRecipe = new Recipe();
            mockRecipe.setId(recipeId);

            UserCollection favorites = new UserCollection();
            favorites.setRecipes(Set.of(mockRecipe));

            when(securityService.getCurrentUser()).thenReturn(mockUser);

            doReturn(favorites).when(mockUser).getFavoritesCollection();

            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            DomainException ex = assertThrows(DomainException.class, () ->
                collectionService.favoriteRecipe(recipeId)
            );

            assertEquals("Recipe is already in your favorites collection", ex.getMessage());
            verify(collectionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("3. Should throw IllegalStateException when favorites collection is null")
        void shouldThrowExceptionWhenCollectionNotInitialized() {
            User mockUser = spy(new User());

            when(securityService.getCurrentUser()).thenReturn(mockUser);

            doReturn(null).when(mockUser).getFavoritesCollection();

            when(recipeService.findByIdOrThrow(anyLong())).thenReturn(new Recipe());

            assertThrows(IllegalStateException.class, () ->
                collectionService.favoriteRecipe(1L)
            );
        }
    }

    @Nested
    @DisplayName("unfavoriteRecipe Tests")
    class UnfavoriteRecipeTests {
        @Test
        @DisplayName("1. Should remove recipe from favorites successfully")
        void shouldRemoveRecipeFromFavorites() {
            Long recipeId = 1L;
            User mockUser = spy(new User());
            Recipe mockRecipe = new Recipe();
            mockRecipe.setId(recipeId);

            UserCollection favorites = new UserCollection();
            favorites.setName("Favorites");

            Set<Recipe> recipes = new HashSet<>();
            recipes.add(mockRecipe);
            favorites.setRecipes(recipes);

            when(securityService.getCurrentUser()).thenReturn(mockUser);
            doReturn(favorites).when(mockUser).getFavoritesCollection();
            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            collectionService.unfavoriteRecipe(recipeId);

            assertFalse(favorites.getRecipes().contains(mockRecipe), "The recipe should be removed");
            verify(collectionRepository, times(1)).saveAndFlush(favorites);
        }

        @Test
        @DisplayName("2. Should throw DomainException when recipe is not in favorites")
        void shouldThrowExceptionWhenNotFavorited() {
            Long recipeId = 1L;
            User mockUser = spy(new User());
            Recipe mockRecipe = new Recipe();
            mockRecipe.setId(recipeId);

            UserCollection favorites = new UserCollection();
            favorites.setRecipes(new HashSet<>());

            when(securityService.getCurrentUser()).thenReturn(mockUser);
            doReturn(favorites).when(mockUser).getFavoritesCollection();
            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            DomainException ex = assertThrows(DomainException.class, () ->
                collectionService.unfavoriteRecipe(recipeId)
            );

            assertEquals("Recipe is not in your favorites collection", ex.getMessage());
            verify(collectionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("3. Should throw IllegalStateException when favorites collection is null")
        void shouldThrowExceptionWhenCollectionNotInitialized() {
            User mockUser = spy(new User());
            when(securityService.getCurrentUser()).thenReturn(mockUser);
            doReturn(null).when(mockUser).getFavoritesCollection();
            when(recipeService.findByIdOrThrow(anyLong())).thenReturn(new Recipe());

            assertThrows(IllegalStateException.class, () ->
                collectionService.unfavoriteRecipe(1L)
            );
        }
    }
}
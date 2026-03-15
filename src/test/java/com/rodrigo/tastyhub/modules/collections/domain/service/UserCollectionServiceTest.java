package com.rodrigo.tastyhub.modules.collections.domain.service;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionSortBy;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.repository.UserCollectionRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    private SecurityService securityService;

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
}
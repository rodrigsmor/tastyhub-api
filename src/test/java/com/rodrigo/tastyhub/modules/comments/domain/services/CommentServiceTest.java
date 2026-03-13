package com.rodrigo.tastyhub.modules.comments.domain.services;

import com.rodrigo.tastyhub.modules.comments.application.dto.request.ReviewRequestDto;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewPagination;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.comments.domain.model.CommentSortBy;
import com.rodrigo.tastyhub.modules.comments.domain.repository.CommentRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RecipeService recipeService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CommentService service;

    @Nested
    @DisplayName("Tests for Review Recipe By Id Method")
    class ReviewRecipeByIdTests {
        @Test
        @DisplayName("Should create and save a review successfully")
        void shouldCreateReviewSuccessfully() {
            Long recipeId = 1L;
            ReviewRequestDto reviewDto = new ReviewRequestDto(
                "Amazing recipe!",
                new BigDecimal("4.0")
            );

            User mockAuthor = new User();
            mockAuthor.setId(10L);

            Recipe mockRecipe = new Recipe();
            mockRecipe.setId(recipeId);

            when(securityService.getCurrentUser()).thenReturn(mockAuthor);
            when(recipeService.findByIdOrThrow(recipeId)).thenReturn(mockRecipe);

            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Comment result = service.reviewRecipeById(recipeId, reviewDto);

            assertNotNull(result);
            assertEquals("Amazing recipe!", result.getContent());
            assertEquals(new BigDecimal("4.0"), result.getRating());
            assertEquals(mockAuthor, result.getUser());
            assertEquals(mockRecipe, result.getRecipe());

            verify(commentRepository, times(1)).save(any(Comment.class));
            verify(recipeService).findByIdOrThrow(recipeId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when recipe does not exist")
        void shouldThrowExceptionWhenRecipeNotFound() {
            Long recipeId = 99L;
            ReviewRequestDto reviewDto = new ReviewRequestDto(
                "Cool",
                new BigDecimal("4.0")
            );

            when(securityService.getCurrentUser()).thenReturn(new User());
            when(recipeService.findByIdOrThrow(recipeId))
                    .thenThrow(new ResourceNotFoundException("Recipe not found"));

            assertThrows(ResourceNotFoundException.class, () ->
                service.reviewRecipeById(recipeId, reviewDto)
            );

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception when SecurityService fails")
        void shouldFailWhenUserNotAuthenticated() {
            Long recipeId = 1L;
            when(securityService.getCurrentUser()).thenThrow(new UnauthorizedException("Not logged in"));

            assertThrows(UnauthorizedException.class, () ->
                service.reviewRecipeById(recipeId, new ReviewRequestDto(
                    "Text",
                    new BigDecimal("4.0")
                ))
            );

            verifyNoInteractions(recipeService);
            verifyNoInteractions(commentRepository);
        }
    }

    @Nested
    @DisplayName("Tests for List Reviews By Recipe Id Method")
    class ListReviewsTests {
        @Test
        @DisplayName("1. Should return ReviewPagination when parameters are valid")
        void shouldReturnReviewPaginationSuccessfully() {
            Long recipeId = 1L;
            int pageNumber = 0;
            int size = 10;

            User author = User
                .builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("chef_johndoe")
                .profilePictureUrl(null)
                .profilePictureAlt(null)
                .build();

            Comment mockComment = Comment
                .builder()
                .user(author)
                .content("Comment content")
                .rating(new BigDecimal("4.5"))
                .build();

            List<Comment> comments = List.of(mockComment);
            Page<Comment> mockPage = new PageImpl<>(comments, PageRequest.of(pageNumber, size), 1);

            when(commentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

            ReviewPagination result = service.listReviewsByRecipeId(
                recipeId, pageNumber, size, CommentSortBy.CREATED_AT, SortDirection.DESC
            );

            assertNotNull(result);
            assertEquals(1, result.metadata().totalItems());
            assertEquals(pageNumber, result.metadata().page());
            assertFalse(result.reviews().isEmpty());

            verify(commentRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("2. Should throw DomainException when recipeId is invalid")
        void shouldThrowExceptionWhenIdIsInvalid() {
            Long invalidId = -1L;

            DomainException ex = assertThrows(DomainException.class, () ->
                service.listReviewsByRecipeId(invalidId, 0, 10, null, null)
            );

            assertEquals("Recipe ID is required", ex.getMessage());
            verifyNoInteractions(commentRepository);
        }

        @Test
        @DisplayName("3. Should correctly map pagination metadata")
        @SuppressWarnings("unchecked")
        void shouldMapMetadataCorrectly() {
            Long recipeId = 1L;
            Page<Comment> mockPage = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 5),
                20
            );

            when(commentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

            ReviewPagination result = service.listReviewsByRecipeId(
                recipeId,
                1,
                5,
                CommentSortBy.RATING,
                SortDirection.ASC
            );

            PaginationMetadata meta = result.metadata();
            assertEquals(1, meta.page());
            assertEquals(4, meta.totalPages());
            assertTrue(meta.hasPrevious());
            assertTrue(meta.hasNext());
        }
    }
}
package com.rodrigo.tastyhub.modules.comments.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.modules.comments.application.dto.request.ReviewRequestDto;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewPagination;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewSummaryDto;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.comments.domain.model.CommentSortBy;
import com.rodrigo.tastyhub.modules.comments.domain.services.CommentService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import static org.mockito.ArgumentMatchers.*;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(CommentController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private static final String BASE_URL = "/api/comments/recipe";

    @Nested
    @DisplayName("POST /api/comments/recipe/{id}")
    class ReviewRecipeTests {
        @Test
        @DisplayName("1. Should return 201 when review is posted successfully")
        void shouldReturn201WhenReviewIsCreated() throws Exception {
            Long recipeId = 1L;
            ReviewRequestDto requestDto = new ReviewRequestDto(
                "Excellent recipe!",
                new BigDecimal("4.0")
            );

            Comment mockComment = new Comment();

            mockComment.setId(500L);
            mockComment.setRating(new BigDecimal("4.0"));

            User author = User
                .builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("chef_johndoe")
                .profilePictureUrl(null)
                .profilePictureAlt(null)
                .build();

            mockComment.setUser(author);
            mockComment.setContent("Excellent recipe!");

            when(commentService.reviewRecipeById(eq(recipeId), any(ReviewRequestDto.class)))
                .thenReturn(mockComment);

            mockMvc.perform(post(BASE_URL + "/{id}", recipeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/recipe/1/500")))
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("2. Should return 400 when rating is outside range")
        void shouldReturn400WhenRatingIsInvalid() throws Exception {
            Long recipeId = 1L;
            ReviewRequestDto invalidDto = new ReviewRequestDto(
                "Too many stars",
                new BigDecimal("5.1")
            );

            mockMvc.perform(post(BASE_URL + "/{id}", recipeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(commentService);
        }

        @Test
        @DisplayName("3. Should return 404 when recipe does not exist")
        void shouldReturn404WhenRecipeNotFound() throws Exception {
            Long recipeId = 999L;
            ReviewRequestDto requestDto = new ReviewRequestDto(
                "Good Content!",
                new BigDecimal("4.0")
            );

            when(commentService.reviewRecipeById(eq(recipeId), any()))
                .thenThrow(new ResourceNotFoundException("Recipe not found with the provided ID"));

            mockMvc.perform(post(BASE_URL + "/{id}", recipeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recipe not found with the provided ID"));
        }
    }

    @Nested
    @DisplayName("GET /api/comments/recipe/{id}")
    class ListRecipeReviewsTests {
        @Test
        @DisplayName("1. Should return 200 and paginated reviews with default values")
        void shouldReturnPaginatedReviewsSuccessfully() throws Exception {
            Long recipeId = 1L;

            ReviewPagination mockPagination = new ReviewPagination(
                List.of(),
                new ReviewSummaryDto(0, 0, 0.0, List.of()),
                new PaginationMetadata(
                    0,
                    10,
                    0,
                    0L,
                    SortDirection.DESC,
                    false,
                    false
                )
            );

            when(commentService.listReviewsByRecipeId(
                eq(recipeId),
                eq(0),
                eq(10),
                eq(CommentSortBy.CREATED_AT), eq(SortDirection.DESC))
            ).thenReturn(mockPagination);

            mockMvc.perform(get("/api/comments/recipe/{id}", recipeId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.page").value(0))
                .andExpect(jsonPath("$.metadata.pageSize").value(10));
        }

        @Test
        @DisplayName("2. Should return 200 when custom parameters are provided")
        void shouldReturnReviewsWithCustomParams() throws Exception {
            Long recipeId = 1L;

            when(commentService.listReviewsByRecipeId(
                eq(recipeId), eq(2), eq(5), eq(CommentSortBy.RATING), eq(SortDirection.ASC))
            ).thenReturn(null);

            mockMvc.perform(get("/api/comments/recipe/{id}", recipeId)
                    .param("page", "2")
                    .param("size", "5")
                    .param("sortBy", "RATING")
                    .param("direction", "ASC")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            verify(commentService).listReviewsByRecipeId(1L, 2, 5, CommentSortBy.RATING, SortDirection.ASC);
        }

        @Test
        @DisplayName("3. Should return 400 when recipeId is invalid (ConstraintViolation)")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get("/api/comments/recipe/{id}", invalidId))
                .andExpect(status().isBadRequest());
        }
    }
}
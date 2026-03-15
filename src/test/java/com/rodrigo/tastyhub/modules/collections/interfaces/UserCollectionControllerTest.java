package com.rodrigo.tastyhub.modules.collections.interfaces;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.request.UserCollectionRequest;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.UserCollectionResponseDto;
import com.rodrigo.tastyhub.modules.collections.domain.service.UserCollectionService;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.ArgumentMatchers.*;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@WebMvcTest(UserCollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCollectionService collectionService;

    @Nested
    @DisplayName("GET /api/collections/user/{id}")
    class ListCollectionsByUserIdTests {

        @Test
        @DisplayName("1. Should return 200 and paginated collections successfully")
        void shouldReturn200WhenParametersAreValid() throws Exception {
            Long userId = 1L;

            CollectionPagination mockPagination = new CollectionPagination(
                List.of(),
                new PaginationMetadata(
                    0,
                    10,
                    0,
                    0L,
                    SortDirection.ASC,
                    false,
                    false
                )
            );

            when(collectionService.listCollectionsByUserId(eq(userId), any(ListCollectionQuery.class)))
                .thenReturn(mockPagination);

            mockMvc.perform(get("/api/collections/user/{id}", userId)
                .param("query", "Pasta")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "NAME")
                .param("direction", "ASC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.totalItems").value(0));
        }

        @Test
        @DisplayName("2. Should return 200 using default query parameters")
        void shouldReturn200WithDefaults() throws Exception {
            Long userId = 1L;
            when(collectionService.listCollectionsByUserId(eq(userId), any()))
                .thenReturn(new CollectionPagination(List.of(), null));

            mockMvc.perform(get("/api/collections/user/{id}", userId))
                .andExpect(status().isOk());

            verify(collectionService).listCollectionsByUserId(eq(userId), any());
        }

        @Test
        @DisplayName("3. Should return 400 when userId is less than 1")
        void shouldReturn400WhenUserIdIsInvalid() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get("/api/collections/user/{id}", invalidId))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(collectionService);
        }

        @Test
        @DisplayName("4. Should return 404 when user does not exist in service")
        void shouldReturn404WhenUserNotFound() throws Exception {
            Long userId = 999L;
            when(collectionService.listCollectionsByUserId(eq(userId), any()))
                .thenThrow(new ResourceNotFoundException("User does not exist or could not be found"));

            mockMvc.perform(get("/api/collections/user/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User does not exist or could not be found"));
        }
    }

    @Nested
    @DisplayName("POST /api/collections")
    class CreateCollectionTests {
        @Test
        @DisplayName("1. Should return 201 when collection is created with image")
        void shouldCreateCollectionSuccessfully() throws Exception {
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

            UserCollectionResponseDto responseDto = new UserCollectionResponseDto(
                100L,
                "Desserts",
                "My favorites",
                "/path/to/img.jpg",
                "Alt text",
                true,
                true,
                false,
                true,
                10,
                0L,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                UserMapper.toSummary(author)
            );

            MockMultipartFile file = new MockMultipartFile(
                "cover",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-data".getBytes()
            );

            MockMultipartFile altText = new MockMultipartFile(
                "alternative_text",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "Accessibility text".getBytes()
            );

            when(collectionService.createCollection(any(UserCollectionRequest.class), any(), anyString()))
                .thenReturn(responseDto);

            mockMvc.perform(multipart("/api/collections")
                .file(file)
                .file(altText)
                .param("name", "Desserts")
                .param("description", "My favorites")
                .param("isPublic", "true")
                .param("isFixed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/collections/100")))
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("Desserts"));
        }

        @Test
        @DisplayName("2. Should return 400 when name is missing (Validation)")
        void shouldReturn400WhenNameIsInvalid() throws Exception {
            mockMvc.perform(multipart("/api/collections")
                .param("description", "Only description")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(collectionService);
        }

        @Test
        @DisplayName("3. Should return 201 when image is optional and not provided")
        void shouldCreateCollectionWithoutImage() throws Exception {
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

            UserCollectionResponseDto responseDto = new UserCollectionResponseDto(
                101L,
                "Salads",
                null,
                null,
                null,
                true,
                false,
                false,
                true,
                10,
                0L,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                UserMapper.toSummary(author)
            );

            when(collectionService.createCollection(any(), eq(null), eq(null)))
                .thenReturn(responseDto);

            mockMvc.perform(multipart("/api/collections")
                .param("name", "Salads")
                .param("isPublic", "true")
                .param("isFixed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.coverUrl").isEmpty());
        }
    }

    @Nested
    @DisplayName("PUT /api/collections/recipe/{id}/favorite")
    class FavoriteRecipeTests {
        @Test
        @DisplayName("1. Should return 204 when recipe is favorited successfully")
        void shouldReturn204WhenFavorited() throws Exception {
            Long recipeId = 1L;
            doNothing().when(collectionService).favoriteRecipe(recipeId);

            mockMvc.perform(put("/api/collections/recipe/{id}/favorite", recipeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            verify(collectionService, times(1)).favoriteRecipe(recipeId);
        }

        @Test
        @DisplayName("2. Should return 400 when recipe is already favorited")
        void shouldReturn400WhenAlreadyFavorited() throws Exception {
            Long recipeId = 1L;
            doThrow(new DomainException("Recipe is already in your favorites collection"))
                .when(collectionService).favoriteRecipe(recipeId);

            mockMvc.perform(put("/api/collections/recipe/{id}/favorite", recipeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Recipe is already in your favorites collection"));
        }

        @Test
        @DisplayName("3. Should return 404 when recipe does not exist")
        void shouldReturn404WhenRecipeNotFound() throws Exception {
            Long recipeId = 999L;
            doThrow(new ResourceNotFoundException("Recipe not found"))
                .when(collectionService).favoriteRecipe(recipeId);

            mockMvc.perform(put("/api/collections/recipe/{id}/favorite", recipeId))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("4. Should return 400 when ID is invalid (zero or negative)")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(put("/api/collections/recipe/{id}/favorite", invalidId))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(collectionService);
        }
    }
}
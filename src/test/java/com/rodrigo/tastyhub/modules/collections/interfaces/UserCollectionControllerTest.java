package com.rodrigo.tastyhub.modules.collections.interfaces;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.domain.service.UserCollectionService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
}
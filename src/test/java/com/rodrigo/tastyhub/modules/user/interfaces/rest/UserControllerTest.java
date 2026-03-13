package com.rodrigo.tastyhub.modules.user.interfaces.rest;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
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

import java.time.LocalDate;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityService securityService;

    @Nested
    @DisplayName("Tests for GET /api/users/{id}")
    class GetProfileTests {
        @Test
        @DisplayName("Should return 200 and profile data when user exists")
        void shouldReturnProfileWhenUserExists() throws Exception {
            Long userId = 1L;
            UserFullStatsDto mockResponse = new UserFullStatsDto(
                userId,
                "Rodrigo",
                "Silva",
                "rodrigo_chef",
                "https://cdn.tastyhub.com/profiles/1.jpg",
                "Rodrigo smiling",
                "Passionate cook",
                "https://cdn.tastyhub.com/covers/1.jpg",
                "Kitchen table",
                LocalDate.of(1995, 5, 15),
                42L, 12L, 1540L, 320L
            );

            when(userService.getUserProfileById(userId)).thenReturn(mockResponse);

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("rodrigo_chef"))
                .andExpect(jsonPath("$.recipeCount").value(42))
                .andExpect(jsonPath("$.followerCount").value(1540));
        }

        @Test
        @DisplayName("Should return 404 when service throws ResourceNotFoundException")
        void shouldReturn404WhenUserNotFound() throws Exception {
            Long userId = 999L;
            when(userService.getUserProfileById(userId))
                .thenThrow(new ResourceNotFoundException("User not found with the provided ID"));

            mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests for PATCH /api/users/profile-picture")
    class UpdateProfilePictureTests {

    }
}
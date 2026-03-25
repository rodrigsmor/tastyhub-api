package com.rodrigo.tastyhub.modules.user.interfaces.rest;

import com.rodrigo.tastyhub.modules.settings.domain.model.ProfileVisibility;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.application.usecases.GetUserProfileUseCase;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

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
    private GetUserProfileUseCase getUserProfile;

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
                ProfileVisibility.PUBLIC,
                "https://cdn.tastyhub.com/profiles/1.jpg",
                "Rodrigo smiling",
                "Passionate cook",
                "https://cdn.tastyhub.com/covers/1.jpg",
                "Kitchen table",
                LocalDate.of(1995, 5, 15),
                true,
                true,
                42L,
                12L,
                1540L,
                320L
            );

            when(getUserProfile.execute(userId)).thenReturn(mockResponse);

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
            when(getUserProfile.execute(userId))
                .thenThrow(new ResourceNotFoundException("User not found with the provided ID"));

            mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests for PATCH /users/profile-picture")
    class UpdateProfilePictureTests {
        @Test
        @DisplayName("1. Should return 200 when profile picture is updated successfully")
        void shouldUpdateProfilePictureSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-content".getBytes()
            );

            MockMultipartFile altText = new MockMultipartFile(
                "alternative_text",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "A profile picture".getBytes()
            );

            UserSummaryDto mockResponse = new UserSummaryDto(
                1L,
                "John",
                "Doe",
                "chef_johndoe",
                "http://cdn.com/1.jpg",
                "A profile picture"
            );

            when(userService.updateProfilePicture(any(MultipartFile.class), anyString()))
                .thenReturn(mockResponse);

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/profile-picture")
                    .file(file)
                    .file(altText)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.profilePictureUrl").value("http://cdn.com/1.jpg"))
                .andExpect(jsonPath("$.profilePictureAlt").value("A profile picture"));
        }

        @Test
        @DisplayName("2. Should return 400 when file is missing or empty")
        void shouldReturn400WhenFileIsEmpty() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
            );

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/profile-picture")
                    .file(emptyFile))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("3. Should return 500 when service fails to store file")
        void shouldReturn500WhenStorageFails() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes()
            );

            when(userService.updateProfilePicture(any(), any()))
                .thenThrow(new RuntimeException("Internal storage failure"));

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/profile-picture")
                    .file(file))
                .andExpect(status().isInternalServerError());
        }
    }
}
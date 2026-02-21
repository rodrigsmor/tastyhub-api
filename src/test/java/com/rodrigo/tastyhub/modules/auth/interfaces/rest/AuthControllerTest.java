package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.LoginResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import com.rodrigo.tastyhub.modules.auth.interfaces.rest.AuthController;
import com.rodrigo.tastyhub.shared.exception.ExpiredTokenException;
import com.rodrigo.tastyhub.shared.exception.InvalidTokenException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("Tests for /auth/signup")
    class SignupTests {
        @Test
        @DisplayName("Should return 201 when signup is successful")
        void signupSuccess() throws Exception {
            SignupRequestDto request = new SignupRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "Pass123!"
            );
            SignupResponseDto response = new SignupResponseDto(
                "Success",
                "john@example.com"
            );

            when(authService.signup(any())).thenReturn(ResponseEntity.created(URI.create("/auth/signup")).body(response));

            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailSentTo").value("john@example.com"));
        }

        @Test
        @DisplayName("Should return 400 when email is already in use")
        void signupFailEmailInUse() throws Exception {
            SignupRequestDto request = new SignupRequestDto(
                "John",
                "Doe",
                "exists@example.com",
                "Pass123!"
            );

            when(authService.signup(any())).thenThrow(new BadRequestException("This email is already in use!"));

            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This email is already in use!"));
        }
    }

    @Nested
    @DisplayName("Tests for /auth/login")
    class LoginTests {
        @Test
        @DisplayName("Should return 200 and tokens when credentials are valid")
        void loginSuccess() throws Exception {
            LoginRequestDto request = new LoginRequestDto(
                "john@email.com",
                "Pass123!"
            );
            LoginResponseDto response = new LoginResponseDto(
                "access",
                "refresh",
                "Bearer "
            );

            when(authService.login(any())).thenReturn(ResponseEntity.ok(response));

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
        }

        @Test
        @DisplayName("Should return 401 when credentials are not valid")
        void shouldReturnBadCredentialsWhenCredentialsAreInvalid() throws Exception {
            LoginRequestDto request = new LoginRequestDto(
                "incorrect-john@example.com",
                "incorrect123!"
            );

            when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Tests for GET /auth/refresh")
    class RefreshTokenTests {
        @Test
        @DisplayName("Should return 200 when refresh token is valid")
        void refreshTokenSuccess() throws Exception {
            String token = "valid-refresh-token";
            LoginResponseDto response = new LoginResponseDto("new-access", "new-refresh", "Bearer ");

            when(authService.refreshToken(token)).thenReturn(ResponseEntity.ok(response));

            mockMvc.perform(get("/auth/refresh")
                    .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
        }

        @Test
        @DisplayName("Should return 400 when token is invalid or expired")
        void refreshTokenFail() throws Exception {
            String token = "invalid-token";

            when(authService.refreshToken(token))
                .thenThrow(new InvalidTokenException("Invalid refresh token."));

            mockMvc.perform(get("/auth/refresh")
                    .param("token", token)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests for GET /auth/verify-email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should return 200 and authenticate user when verification is valid")
        void verifyEmailSuccess() throws Exception {
            String token = "valid-verify-token";
            LoginResponseDto response = new LoginResponseDto("access", "refresh", "Bearer ");

            when(authService.verifyEmail(token)).thenReturn(ResponseEntity.ok(response));

            mockMvc.perform(get("/auth/verify-email")
                    .param("token", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
        }

        @Test
        @DisplayName("Should return 410 (Gone) or 401 when link is expired")
        void verifyEmailExpired() throws Exception {
            String token = "expired-token";

            when(authService.verifyEmail(token))
                .thenThrow(new ExpiredTokenException("This verification link has expired."));

            mockMvc.perform(get("/auth/verify-email")
                    .param("token", token))
                .andExpect(status().isUnauthorized());
        }
    }
}
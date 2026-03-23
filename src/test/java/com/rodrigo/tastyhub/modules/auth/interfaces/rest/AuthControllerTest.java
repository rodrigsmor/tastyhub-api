package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.AuthResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.usecases.*;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ExpiredTokenException;
import com.rodrigo.tastyhub.shared.exception.InvalidTokenException;
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
    private LoginUseCase loginUseCase;

    @MockitoBean
    private SignupUseCase signupUseCase;

    @MockitoBean
    private GetMyProfileUseCase useCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @MockitoBean
    private LogOutUseCase logout;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("Tests for /api/auth/signup")
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

            when(signupUseCase.execute(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/signup")
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

            when(signupUseCase.execute(any())).thenThrow(new DomainException("This email is already in use!"));

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This email is already in use!"));
        }
    }

    @Nested
    @DisplayName("Tests for /api/auth/login")
    class LoginTests {
        @Test
        @DisplayName("Should return 200 and tokens when credentials are valid")
        void loginSuccess() throws Exception {
            LoginRequestDto request = new LoginRequestDto(
                "john@email.com",
                "Pass123!"
            );
            AuthResponseDto response = new AuthResponseDto(
                "access",
                "refresh",
                "Bearer "
            );

            when(loginUseCase.execute(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
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

            when(loginUseCase.execute(any(LoginRequestDto.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Tests for /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should return 204 when logout is successful")
        void logoutSuccess() throws Exception {
            String refreshToken = "valid-refresh-token";

            doNothing().when(logout).execute(refreshToken);

            mockMvc.perform(post("/api/auth/logout")
                    .header("X-Refresh-Token", refreshToken)
                    .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isNoContent());

            verify(logout, times(1)).execute(refreshToken);
        }
        @Test
        @DisplayName("Should return 400 when refresh token is invalid")
        void logoutWithInvalidToken() throws Exception {
            String invalidToken = "invalid-token";

            doThrow(new IllegalArgumentException("Invalid refresh token."))
                .when(logout).execute(invalidToken);

            mockMvc.perform(post("/api/auth/logout")
                    .header("X-Refresh-Token", invalidToken))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when X-Refresh-Token header is missing")
        void logoutMissingHeader() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests for GET /api/auth/refresh-token")
    class RefreshTokenTests {
        @Test
        @DisplayName("Should return 200 when refresh token is valid")
        void refreshTokenSuccess() throws Exception {
            String refreshToken = "valid-refresh-token";
            AuthResponseDto response = new AuthResponseDto("new-access", "new-refresh", "Bearer ");

            when(refreshTokenUseCase.execute(refreshToken)).thenReturn(response);

            mockMvc.perform(post("/api/auth/refresh-token")
                    .header("X-Refresh-Token", refreshToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
        }

        @Test
        @DisplayName("Should return 400 when token is invalid or expired")
        void refreshTokenFail() throws Exception {
            String refreshToken = "invalid-token";

            when(refreshTokenUseCase.execute(refreshToken))
                .thenThrow(new InvalidTokenException("Invalid refresh token."));

            mockMvc.perform(post("/api/auth/refresh-token")
                    .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests for GET /api/auth/verify-email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should return 200 and authenticate user when verification is valid")
        void verifyEmailSuccess() throws Exception {
            String token = "valid-verify-token";
            AuthResponseDto response = new AuthResponseDto("access", "refresh", "Bearer ");

            when(verifyEmailUseCase.execute(token)).thenReturn(response);

            mockMvc.perform(
                    get("/api/auth/verify-email/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
        }

        @Test
        @DisplayName("Should return 410 (Gone) or 401 when link is expired")
        void verifyEmailExpired() throws Exception {
            String token = "expired-token";

            when(verifyEmailUseCase.execute(token))
                .thenThrow(new ExpiredTokenException("This verification link has expired."));

            mockMvc.perform(get("/api/auth/verify-email/{token}", token))
                .andExpect(status().isUnauthorized());
        }
    }
}
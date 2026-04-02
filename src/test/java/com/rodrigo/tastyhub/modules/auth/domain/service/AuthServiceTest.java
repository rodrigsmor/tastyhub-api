package com.rodrigo.tastyhub.modules.auth.domain.service;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.AuthResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.repository.RefreshTokenRepository;
import com.rodrigo.tastyhub.modules.auth.domain.repository.VerificationTokenRepository;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.exception.*;
import com.rodrigo.tastyhub.modules.auth.infrastructure.JwtGenerator;
import com.rodrigo.tastyhub.modules.auth.domain.model.RefreshToken;
import com.rodrigo.tastyhub.modules.auth.domain.model.VerificationToken;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.model.UserStatus;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private JwtGenerator jwtGenerator;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Tests for Login method")
    class LoginTests {
        @Test
        @DisplayName("Should throw BadCredentialsException when email or password is wrong")
        void shouldThrowBadCredentialsExceptionWhenAuthenticationFails() {
            LoginRequestDto loginDto = new LoginRequestDto(
                "wrong@email.com",
                "wrongpass"
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

            assertThrows(BadCredentialsException.class, () -> {
                authService.login(loginDto);
            });

            verify(jwtGenerator, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should throw BadCredential when user record is not found")
        void shouldThrowBadCredentialWhenUserIsNotFound() {
            LoginRequestDto loginDto = new LoginRequestDto(
                "wrong@email.com",
                "wrongpass"
            );

            User user = new User();
            user.setEmail(loginDto.email());
            user.setStatus(UserStatus.ACTIVE);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.emptyList()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(userRepository.findByEmail(loginDto.email()))
                .thenThrow(new BadCredentialsException("User record not found"));

            assertThrows(BadCredentialsException.class, () -> {
                authService.login(loginDto);
            });

            verify(jwtGenerator, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should return access and refresh tokens when credentials are valid")
        void shouldLoginSuccessfully() {
            LoginRequestDto loginDto = new LoginRequestDto(
                "wrong@email.com",
                "wrongpass"
            );

            User user = new User();
            user.setEmail(loginDto.email());
            user.setStatus(UserStatus.ACTIVE);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.emptyList()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(userRepository.findByEmail(loginDto.email())).thenReturn(Optional.of(user));
            when(jwtGenerator.generateToken(authentication)).thenReturn("mocked-jwt-token");

            AuthResponseDto response = authService.login(loginDto);

            assertNotNull(response.accessToken());

            assertEquals("mocked-jwt-token", response.accessToken());
            assertNotNull(response.refreshToken());

            assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Tests for Log Out method")
    class LogOutTests {

        @Mock
        private RefreshTokenRepository refreshTokenRepository;

        @InjectMocks
        private AuthService authService;

        @Test
        @DisplayName("Should successfully delete refresh token when it exists")
        void shouldLogOutSuccessfully() throws BadRequestException {
            String refreshToken = "valid-token-123";
            RefreshToken tokenEntity = new RefreshToken();
            tokenEntity.setToken(refreshToken);

            when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(tokenEntity));

            authService.logOut(refreshToken);

            verify(refreshTokenRepository, times(1)).delete(tokenEntity);
            verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        }

        @Test
        @DisplayName("Should throw BadRequestException when token is not found")
        void shouldThrowExceptionWhenTokenNotFound() {
            String refreshToken = "non-existent-token";
            when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                authService.logOut(refreshToken);
            });

            assertEquals("Refresh token is missing or invalid", exception.getMessage());
            verify(refreshTokenRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Tests for Refresh Token method")
    class refreshTokenTests {
        @Test
        @DisplayName("Should throw InvalidToken when the provided token is not recorded")
        void shouldThrowInvalidTokenWhenTokenIsNotRecorded() {
            String token = "wrong-token";

            when(refreshTokenRepository.findByToken(token))
                .thenThrow(new InvalidTokenException("Invalid refresh token."));

            assertThrows(InvalidTokenException.class, () -> {
                authService.refreshToken(token);
            });

            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ExpiredToken when refresh token is expired")
        void shouldThrowExpiredTokenWhenRefreshTokenIsExpired() {
            String token = "refresh-token";

            User user = new User();

            LocalDateTime pastDate = LocalDateTime.now().minusDays(7);

            RefreshToken refreshToken = new RefreshToken(
                0L,
                token,
                user,
                pastDate,
                false
            );

            when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

            assertThrows(ExpiredTokenException.class, () -> {
                authService.refreshToken(token);
            });

            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ExpiredToken when refresh token is revoked")
        void shouldThrowExpiredTokenWhenRefreshTokenIsRevoked() {
            String token = "refresh-token";

            User user = new User();

            LocalDateTime nextMonth = LocalDateTime.now().plusMonths(1);

            RefreshToken refreshToken = new RefreshToken(
                0L,
                token,
                user,
                nextMonth,
                true
            );

            when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

            assertThrows(ExpiredTokenException.class, () -> {
                authService.refreshToken(token);
            });

            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should refresh token successfully when token is valid")
        void refreshTokenSuccess() {
            String oldTokenValue = "old-valid-refresh-token";
            User user = new User();
            user.setEmail("user@example.com");

            RefreshToken refreshToken = new RefreshToken(
                0L,
                oldTokenValue,
                user,
                LocalDateTime.now().plusMonths(1),
                false
            );


            when(refreshTokenRepository.findByToken(oldTokenValue))
                .thenReturn(Optional.of(refreshToken));

            when(jwtGenerator.generateToken(any(Authentication.class)))
                .thenReturn("new-access-token");

            AuthResponseDto response = authService.refreshToken(oldTokenValue);

            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());

            verify(refreshTokenRepository).delete(refreshToken);

            verify(refreshTokenRepository).save(any(RefreshToken.class));

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(user.getEmail(), SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    @Nested
    @DisplayName("Tests for Verify E-mail method")
    class verifyEmailTests {
        @Test
        @DisplayName("Should throw InvalidToken when the email's verification token is not recorded")
        void shouldThrowInvalidTokenWhenTokenIsNotRecorded() {
            String token = "wrong-token";

            when(verificationTokenRepository.findByToken(token))
                .thenThrow(new InvalidTokenException("Invalid refresh token."));

            assertThrows(InvalidTokenException.class, () -> {
                authService.verifyEmail(token);
            });

            verify(verificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ExpiredToken when verification token is expired")
        void shouldThrowExpiredTokenWhenRefreshTokenIsExpired() {
            String token = "refresh-token";

            User user = new User();

            LocalDateTime pastDate = LocalDateTime.now().minusDays(7);

            VerificationToken verificationToken = new VerificationToken(
                0L,
                token,
                user,
                pastDate
            );

            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

            assertThrows(ExpiredTokenException.class, () -> {
                authService.verifyEmail(token);
            });

            verify(verificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should verify email successfully and start onboarding")
        void verifyEmailSuccess() {
            String validToken = "valid-uuid-token";

            User user = new User();
            user.setEmail("rodrigo@tastyhub.com");
            user.setStatus(UserStatus.ACTIVE);

            VerificationToken vToken = new VerificationToken();
            vToken.setToken(validToken);
            vToken.setUser(user);
            vToken.setExpiryDate(LocalDateTime.now().plusHours(24));

            when(verificationTokenRepository.findByToken(validToken))
                .thenReturn(Optional.of(vToken));

            when(jwtGenerator.generateToken(any(Authentication.class)))
                .thenReturn("generated-access-token");

            AuthResponseDto response = authService.verifyEmail(validToken);

            assertNotNull(response);
            assertEquals("generated-access-token", response.accessToken());

            assertNotEquals(UserStatus.PENDING, user.getStatus());

            verify(verificationTokenRepository).delete(vToken);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }
}
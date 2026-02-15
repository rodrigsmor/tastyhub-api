package com.rodrigo.tastyhub.domain.service;

import com.rodrigo.tastyhub.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.domain.model.Role;
import com.rodrigo.tastyhub.domain.model.User;
import com.rodrigo.tastyhub.domain.model.UserRole;
import com.rodrigo.tastyhub.domain.repository.RefreshTokenRepository;
import com.rodrigo.tastyhub.domain.repository.RoleRepository;
import com.rodrigo.tastyhub.domain.repository.UserRepository;
import com.rodrigo.tastyhub.domain.repository.VerificationTokenRepository;
import com.rodrigo.tastyhub.exceptions.InfrastructureException;
import com.rodrigo.tastyhub.infrastructure.security.JwtGenerator;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
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
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Tests for Sign Up Method")
    class SignupTests {
        @Test
        @DisplayName("Should throws BadRequest when e-mail is already in use")
        void shouldThrowsBadRequestExceptionWhenEmailIsAlreadyInUse() {
            SignupRequestDto signupDto = new SignupRequestDto(
                "Mary",
                "Smith",
                "mary.smith@example.com",
                "123Password!"
            );

            when(userRepository.existsByUsername(signupDto.email())).thenReturn(true);

            assertThrows(BadRequestException.class, () -> {
                authService.signup(signupDto);
            });
        }

        @Test
        @DisplayName("Should throws Infrastructure when default role is not found")
        void shouldThrowsInfrastructureExceptionWhenDefaultRoleIsNotFound() {
            SignupRequestDto signupDto = new SignupRequestDto(
                "Mary",
                "Smith",
                "mary.smith@example.com",
                "123Password!"
            );

            when(userRepository.existsByUsername(signupDto.email())).thenReturn(false);

            assertThrows(InfrastructureException.class, () -> {
                authService.signup(signupDto);
            });
        }

        @Test
        @DisplayName("Should create New User when credentials are valid")
        void shouldCreateUserSuccessfully() throws BadRequestException {
            SignupRequestDto signupDto = new SignupRequestDto(
                "Mary",
                "Smith",
                "mary.smith@example.com",
                "123Password!"
            );

            Role userRole = new Role(1L, UserRole.ROLE_USER);

            when(userRepository.existsByUsername(signupDto.email())).thenReturn(false);
            when(roleRepository.findByName(UserRole.ROLE_USER)).thenReturn(Optional.of(userRole));

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ResponseEntity<SignupResponseDto> response = authService.signup(signupDto);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            assertTrue(response.getHeaders().getLocation().toString().contains("/auth/signup"));

            SignupResponseDto body = response.getBody();
            assertNotNull(body);
            assertEquals(signupDto.email(), body.emailSentTo());

            assertTrue(body.message().contains("Verify your account!"));

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Tests for login method")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully when credentials are valid")
        void loginSuccess() {
            // Arrange (Given)
            // Act (When)
            // Assert (Then)
        }

        @Test
        @DisplayName("Should throw AuthException when password does not match")
        void loginInvalidPassword() {

        }
    }

    @Test
    void refreshToken() {
    }

    @Test
    void verifyEmail() {
    }
}
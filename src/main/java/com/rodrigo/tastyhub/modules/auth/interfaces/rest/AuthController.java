package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.LoginResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints for identity management, user registration, and access control.")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Register a new user",
        description = "Initiates the account creation process by validating user credentials and triggering a mandatory email verification flow for account onboarding.",
        tags = { "Authentication" }
    )
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupDto) throws BadRequestException {
        return authService.signup(signupDto);
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates user credentials to provide access and refresh tokens for secure session management.",
        tags = { "Authentication" }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        return authService.login(loginDto);
    }

    @Operation(
        summary = "Refresh access token",
        description = "Exchanges a valid refresh token for a new access token to extend the user session without re-authentication.",
        tags = { "Authentication" }
    )
    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestParam String token) {
        return authService.refreshToken(token);
    }

    @Operation(
        summary = "Verify user email",
        description = "Validates the user's email address using a verification token to activate the account and ensure identity veracity.",
        tags = { "Authentication" }
    )
    @GetMapping("/verify-email")
    public ResponseEntity<LoginResponseDto> verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }
}

package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.LoginResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account successfully created! Please, verify your account."),
        @ApiResponse(responseCode = "400", description = "This email is already in use!"),
        @ApiResponse(responseCode = "500", description = "Critical Error: Default Role not found in database!"),
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupDto) throws BadRequestException {
        return authService.signup(signupDto);
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates user credentials to provide access and refresh tokens for secure session management.",
        tags = { "Authentication" }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully logged in"),
        @ApiResponse(responseCode = "400", description = "User record not found"),
        @ApiResponse(responseCode = "403", description = "Please verify your email before logging in")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        return authService.login(loginDto);
    }

    @Operation(
        summary = "Refresh access token",
        description = "Exchanges a valid refresh token for a new access token to extend the user session without re-authentication.",
        tags = { "Authentication" }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The token has been successfully refreshed."),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token."),
        @ApiResponse(responseCode = "401", description = "Refresh token expired or revoked. Please log in again."),
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
        @RequestHeader(name = "X-Refresh-Token", required = true) String refreshToken
    ) {
        return authService.refreshToken(refreshToken);
    }

    @Operation(
        summary = "Verify user email",
        description = "Validates the user's email address using a verification token to activate the account and ensure identity veracity.",
        tags = { "Authentication" }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The token has been successfully refreshed."),
        @ApiResponse(responseCode = "400", description = "Invalid or missing verification token."),
        @ApiResponse(responseCode = "401", description = "This verification link has expired."),
    })
    @GetMapping("/verify-email/{token}")
    public ResponseEntity<LoginResponseDto> verifyEmail(@PathVariable String token) {
        return authService.verifyEmail(token);
    }

    @Operation(
        summary = "Logout user and invalidate session",
        description = "Removes the refresh token from the database, preventing new access tokens from being generated.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully logged out"),
        @ApiResponse(responseCode = "400", description = "Refresh token is missing or invalid")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader(name = "X-Refresh-Token", required = true) String refreshToken
    ) throws BadRequestException {
        return authService.logOut(refreshToken);
    }
}

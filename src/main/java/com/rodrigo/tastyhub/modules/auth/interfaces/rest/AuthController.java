package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.AuthResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.application.usecases.*;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Authentication", description = "Endpoints for identity management, user registration, and access control.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginUseCase login;
    private final LogOutUseCase logout;
    private final SignupUseCase signup;
    private final RefreshTokenUseCase refreshToken;
    private final GetMyProfileUseCase getMyProfile;
    private final VerifyEmailUseCase verifyEmail;

    public AuthController(
        LoginUseCase login,
        SignupUseCase signup,
        LogOutUseCase logout,
        RefreshTokenUseCase refreshToken,
        VerifyEmailUseCase verifyEmail,
        GetMyProfileUseCase getMyProfile
    ) {
        this.login = login;
        this.logout = logout;
        this.signup = signup;
        this.refreshToken = refreshToken;
        this.getMyProfile = getMyProfile;
        this.verifyEmail = verifyEmail;
    }

    @Operation(
        summary = "Get current user profile",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Returns full profile data and statistics for the authenticated user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User Not Found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    @GetMapping("/me")
    public ResponseEntity<UserFullStatsDto> getMyProfile() {
        UserFullStatsDto response = this.getMyProfile.execute();
        return ResponseEntity.ok(response);
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
        SignupResponseDto response = this.signup.execute(signupDto);

        URI uri = URI.create(ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/auth/signup")
            .toUriString()
        );

        return ResponseEntity.created(uri).body(response);
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
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        AuthResponseDto response = login.execute(loginDto);
        return ResponseEntity.ok(response);
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
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(
        @RequestHeader(name = "X-Refresh-Token", required = true) String refreshToken
    ) {
        AuthResponseDto response = this.refreshToken.execute(refreshToken);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<AuthResponseDto> verifyEmail(@PathVariable String token) {
        AuthResponseDto response = verifyEmail.execute(token);
        return ResponseEntity.ok(response);
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
    ) {
        logout.execute(refreshToken);
        return ResponseEntity.noContent().build();
    }
}

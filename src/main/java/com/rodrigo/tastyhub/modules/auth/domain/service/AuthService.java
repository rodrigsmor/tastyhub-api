package com.rodrigo.tastyhub.modules.auth.domain.service;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.LoginResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.repository.RefreshTokenRepository;
import com.rodrigo.tastyhub.modules.auth.domain.repository.VerificationTokenRepository;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.exception.*;
import com.rodrigo.tastyhub.modules.auth.infrastructure.JwtGenerator;
import com.rodrigo.tastyhub.modules.auth.domain.model.RefreshToken;
import com.rodrigo.tastyhub.modules.auth.domain.model.VerificationToken;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    public User getVerifiedUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("User record not found"));

        if (!user.isVerified()) {
            throw new ForbiddenException("Please verify your email before logging in");
        }

        return user;
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );

        User user = this.getVerifiedUserByEmail(loginDto.email());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtGenerator.generateToken(authentication);
        String refreshToken = createAndSaveRefreshToken(user);

        return new LoginResponseDto(
            accessToken,
            refreshToken,
            user.getOnboardingStatus().name()
        );
    }

    @Transactional
    public ResponseEntity<Void> logOut(String refreshToken) throws BadRequestException {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new BadRequestException("Refresh token is missing or invalid"));

        refreshTokenRepository.delete(tokenEntity);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token."));

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new ExpiredTokenException("Refresh token expired or revoked. Please log in again.");
        }

        User user = refreshToken.getUser();

        refreshTokenRepository.delete(refreshToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> verifyEmail(String token) {
        VerificationToken vToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid or missing verification token."));

        if (vToken.isExpired()) {
            throw new ExpiredTokenException("This verification link has expired.");
        }

        User user = vToken.getUser();
        onboardingService.startOnboarding(user);

        verificationTokenRepository.delete(vToken);
        return generateAuthResponse(user);
    }

    private ResponseEntity<LoginResponseDto> generateAuthResponse(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            user.getAuthorities()
        );

        String accessToken = jwtGenerator.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String refreshToken = createAndSaveRefreshToken(user);

        return ResponseEntity.ok(new LoginResponseDto(
            accessToken,
            refreshToken,
            "Bearer "
        ));
    }

    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(24))
            .build();

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    private String createAndSaveRefreshToken(User user) {
        String newToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
            .token(newToken)
            .user(user)
            .expiryDate(LocalDateTime.now().plusDays(7))
            .build();
        refreshTokenRepository.save(refreshToken);
        return newToken;
    }
}

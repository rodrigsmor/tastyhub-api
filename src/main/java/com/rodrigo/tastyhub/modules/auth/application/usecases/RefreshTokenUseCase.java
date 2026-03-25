package com.rodrigo.tastyhub.modules.auth.application.usecases;

import com.rodrigo.tastyhub.modules.auth.application.dto.response.AuthResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {
    private final AuthService authService;

    public AuthResponseDto execute(String token) {
        return this.authService.refreshToken(token);
    }
}

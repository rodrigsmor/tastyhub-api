package com.rodrigo.tastyhub.modules.auth.application.usecases;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.AuthResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {
    private final AuthService authService;

    public AuthResponseDto execute(LoginRequestDto loginRequestDto) {
        return this.authService.login(loginRequestDto);
    }
}

package com.rodrigo.tastyhub.modules.auth.application.usecases;

import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogOutUseCase {
    private final AuthService authService;

    public void execute(String refreshToken) {
        this.authService.logOut(refreshToken);
    }
}

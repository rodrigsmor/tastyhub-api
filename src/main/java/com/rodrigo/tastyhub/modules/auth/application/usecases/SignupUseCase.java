package com.rodrigo.tastyhub.modules.auth.application.usecases;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.auth.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.modules.auth.domain.service.AuthService;
import com.rodrigo.tastyhub.modules.user.domain.model.Role;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupUseCase {
    private final AuthService authService;
    private final RoleService roleService;

    public SignupResponseDto execute(SignupRequestDto request) {
        Role role = roleService.getDefaultRole();

        User user = this.authService.signup(request, role);

        String verificationToken = this.authService.createVerificationToken(user);

        return new SignupResponseDto(
            "Account successfully created! Please, verify your account. (temporary) verification code: " + verificationToken,
            user.getEmail()
        );
    }
}

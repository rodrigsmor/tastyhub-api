package com.rodrigo.tastyhub.interfaces.rest.controllers;

import com.rodrigo.tastyhub.application.dto.request.LoginRequestDto;
import com.rodrigo.tastyhub.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.application.dto.response.LoginResponseDto;
import com.rodrigo.tastyhub.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.domain.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupDto) throws BadRequestException {
        return authService.signup(signupDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        return authService.login(loginDto);
    }
}

package com.rodrigo.tastyhub.interfaces.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.application.dto.response.SignupResponseDto;
import com.rodrigo.tastyhub.domain.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("Tests for /auth/signup")
    class SignupTests {
        @Test
        @DisplayName("Should return 201 when signup is successful")
        void signupSuccess() throws Exception {
            SignupRequestDto request = new SignupRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "Pass123!"
            );
            SignupResponseDto response = new SignupResponseDto(
                "Success",
                "john@example.com"
            );

            when(authService.signup(any())).thenReturn(ResponseEntity.created(URI.create("/auth/signup")).body(response));

            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailSentTo").value("john@example.com"));
        }

        @Test
        @DisplayName("Should return 400 when email is already in use")
        void signupFailEmailInUse() throws Exception {
            SignupRequestDto request = new SignupRequestDto(
                "John",
                "Doe",
                "exists@example.com",
                "Pass123!"
            );

            when(authService.signup(any())).thenThrow(new BadRequestException("This email is already in use!"));

            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This email is already in use!"));
        }
    }
}
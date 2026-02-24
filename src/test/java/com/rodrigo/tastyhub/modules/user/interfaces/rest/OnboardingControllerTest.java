package com.rodrigo.tastyhub.modules.user.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.OnBoardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OnboardingController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class OnboardingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OnboardingService onboardingService;

    @MockitoBean
    private SecurityService securityService;

    @Nested
    @DisplayName("Tests for PATCH /api/onboarding/profile")
    class ProfileTests {

        @Test
        @DisplayName("Should update profile successfully and return 200 OK")
        void shouldUpdateProfileSuccessfully() throws Exception {
            OnboardingProgressDto expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.STEP_2,
                OnBoardingStatus.STEP_3,
                false
            );

            when(onboardingService.updateUserProfile(any(), any()))
                .thenReturn(ResponseEntity.ok(expectedResponse));

            MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "content".getBytes()
            );

            mockMvc.perform(
                multipart(HttpMethod.PATCH, "/api/onboarding/profile")
                    .file(file)
                    .param("username", "johndoe")
                    .param("bio", "Chef and foodie")
                    .param("alternativeText", "A profile picture")
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("STEP_2"));
        }

        @Test
        @DisplayName("Should return 400 when validation fails (username empty)")
        void shouldReturn400WhenUsernameIsEmpty() throws Exception {
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/onboarding/profile")
                    .param("username", "")
                    .param("bio", "Some bio"))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(onboardingService);
        }

        @Test
        @DisplayName("Should allow updating without a file")
        void shouldAllowUpdateWithoutFile() throws Exception {
            OnboardingProgressDto expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.STEP_2,
                OnBoardingStatus.STEP_3,
                false
            );

            when(onboardingService.updateUserProfile(any(), isNull()))
                    .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/onboarding/profile")
                            .param("username", "johndoe")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Tests for POST /api/onboarding/interests")
    class InterestsTests {

        @Test
        @DisplayName("1. Should select interests successfully and return 200 OK")
        void shouldSelectInterestsSuccessfully() throws Exception {
            var request = new OnboardingInterestsRequest(Set.of(1L, 2L), Set.of("vegan"), null);
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.STEP_3,
                OnBoardingStatus.COMPLETED,
                false
            );

            when(onboardingService.selectInterests(any(), anyBoolean()))
                    .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(post("/api/onboarding/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("STEP_3"));
        }

        @Test
        @DisplayName("2. Should allow skipping the step when shouldSkip is true")
        void shouldAllowSkippingStep() throws Exception {
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.STEP_3,
                OnBoardingStatus.COMPLETED,
                false
            );

            when(onboardingService.selectInterests(any(), eq(true)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(post("/api/onboarding/interests")
                    .param("shouldSkip", "true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("STEP_3"));
        }

        @Test
        @DisplayName("3. Should return 400 when unknown fields are sent (Strict JSON)")
        void shouldReturn400WhenUnknownFieldSent() throws Exception {
            String jsonWithExtraField = """
                {
                    "tagIds": [1],
                    "unknownField": "hack"
                }
                """;

            mockMvc.perform(post("/api/onboarding/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithExtraField))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Unknown field detected")));
        }

        @Test
        @DisplayName("4. Should return 400 when JSON is malformed")
        void shouldReturn400WhenJsonIsMalformed() throws Exception {
            String malformedJson = "{ \"tagIds\": [1, 2 ";

            mockMvc.perform(post("/api/onboarding/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON")));
        }

        @Test
        @DisplayName("5. Should return 400 when field types are invalid (e.g., String in Long list)")
        void shouldReturn400WhenInvalidTypesSent() throws Exception {
            String invalidTypeJson = """
                {
                    "tagIds": ["not-a-number"]
                }
                """;

            mockMvc.perform(post("/api/onboarding/interests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid format for field")));
        }
    }

    @Nested
    @DisplayName("Tests for POST /api/onboarding/connections")
    class ConnectionsTests {

        @Test
        @DisplayName("1. Should complete onboarding successfully and return COMPLETED status")
        void shouldCompleteOnboardingSuccessfully() throws Exception {
            var request = new OnboardingConnectionsRequest(Set.of(10L, 11L, 12L));
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.COMPLETED,
                OnBoardingStatus.COMPLETED,
                true
            );

            when(onboardingService.followInitialUsers(any(), eq(false)))
                    .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(post("/api/onboarding/connections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("2. Should allow skipping connections and still complete onboarding")
        void shouldAllowSkippingConnections() throws Exception {
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.COMPLETED,
                OnBoardingStatus.COMPLETED,
                true
            );

            when(onboardingService.followInitialUsers(any(), eq(true)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(post("/api/onboarding/connections")
                    .param("shouldSkip", "true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("3. Should return 400 when connection request body is missing")
        void shouldReturn400WhenBodyIsMissing() throws Exception {
            mockMvc.perform(post("/api/onboarding/connections")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
        }

        @Test
        @DisplayName("4. Should handle empty user list gracefully")
        void shouldHandleEmptyUserList() throws Exception {
            var request = new OnboardingConnectionsRequest(Set.of());
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.COMPLETED,
                OnBoardingStatus.COMPLETED,
                true
            );

            when(onboardingService.followInitialUsers(any(), eq(false)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(post("/api/onboarding/connections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("5. Should return 400 when user IDs are in wrong format (e.g. Strings)")
        void shouldReturn400ForInvalidIdFormat() throws Exception {
            String invalidJson = "{ \"userIds\": [\"not-a-long\"] }";

            mockMvc.perform(post("/api/onboarding/connections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid format")));
        }
    }

    @Nested
    @DisplayName("Tests for PATCH /api/onboarding/back")
    class BackStepTests {
        @Test
        @DisplayName("1. Should revert from STEP_2 to STEP_1 successfully")
        void shouldGoBackSuccessfully() throws Exception {
            var expectedResponse = new OnboardingProgressDto(
                OnBoardingStatus.STEP_1,
                OnBoardingStatus.STEP_2,
                false
            );

            when(onboardingService.backToPreviousStep())
                .thenReturn(ResponseEntity.ok(expectedResponse));

            mockMvc.perform(patch("/api/onboarding/back"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("STEP_1"))
                .andExpect(jsonPath("$.isCompleted").value(false));
        }

        @Test
        @DisplayName("2. Should return 400 when trying to go back from the first step")
        void shouldReturn400WhenAlreadyAtFirstStep() throws Exception {
            when(onboardingService.backToPreviousStep())
                    .thenThrow(new BadRequestException("Already at the initial onboarding step"));

            mockMvc.perform(patch("/api/onboarding/back"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("initial onboarding step")));
        }

        @Test
        @DisplayName("3. Should return 403 when trying to revert a completed onboarding")
        void shouldReturn403WhenOnboardingIsCompleted() throws Exception {
            when(onboardingService.backToPreviousStep())
                .thenThrow(new ForbiddenException("Onboarding has already been completed"));

            mockMvc.perform(patch("/api/onboarding/back"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("already been completed")));
        }

        @Test
        @DisplayName("4. Should return 401 when user is not verified")
        void shouldReturn401WhenNotVerified() throws Exception {
            when(onboardingService.backToPreviousStep())
                .thenThrow(new ForbiddenException("Account not verified"));

            mockMvc.perform(patch("/api/onboarding/back"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Tests for GET /api/onboarding/step")
    class GetStepTests {

        @Test
        @DisplayName("Should return current onboarding status successfully")
        void shouldReturnCurrentStep() throws Exception {
            OnboardingProgressDto expectedResponse = new OnboardingProgressDto(
                    OnBoardingStatus.STEP_2,
                    OnBoardingStatus.STEP_3,
                    false
            );

            when(onboardingService.getCurrentStep()).thenReturn(expectedResponse);

            mockMvc.perform(get("/api/onboarding/step")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("STEP_2"))
                .andExpect(jsonPath("$.nextStepAction").value("STEP_3"))
                .andExpect(jsonPath("$.isCompleted").value(false));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenAnonymous() throws Exception {
            when(onboardingService.getCurrentStep())
                .thenThrow(new UnauthorizedException("Unauthorized - User must be logged in"));

            mockMvc.perform(get("/api/onboarding/step"))
                .andExpect(status().isUnauthorized());
        }
    }
}
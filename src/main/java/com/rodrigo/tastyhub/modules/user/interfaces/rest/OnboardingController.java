package com.rodrigo.tastyhub.modules.user.interfaces.rest;

import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingIdentityRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresOnboardingStep;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(
    name = "Onboarding",
    description = "Handles the multi-step user onboarding process, including identity setup, interest selection, and initial social connections."
)
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @Operation(
        summary = "Step 1: Setup Profile Identity",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Updates the user's basic profile information such as username, bio, and profile picture. Moves the user to STEP_2."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Identity updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already taken"),
        @ApiResponse(responseCode = "403", description = "Access denied: User is not in STEP_1")
    })
    @PatchMapping(
        value = "/profile",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @RequiresOnboardingStep(OnboardingStatus.STEP_1)
    public ResponseEntity<OnboardingProgressDto> updateBasicProfileInformation(
        @Parameter(description = "Profile data in JSON format")
        @Valid @ModelAttribute OnboardingIdentityRequest request,

        @Parameter(description = "Profile picture file")
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return this.onboardingService.updateUserProfile(request, file);
    }

    @Operation(
        summary = "Step 2: Select Interests and Tags",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Allows the user to follow existing tags, create new ones, or skip this step. Moves the user to STEP_3."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Interests updated or step skipped successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied: User is not in STEP_2")
    })
    @PostMapping("/interests")
    @RequiresOnboardingStep(OnboardingStatus.STEP_2)
    public ResponseEntity<OnboardingProgressDto> selectInterests(
        @Valid @RequestBody OnboardingInterestsRequest request,
        @RequestParam(value = "shouldSkip", defaultValue = "false") boolean shouldSkip
    ) {
        return this.onboardingService.selectInterests(request, shouldSkip);
    }

    @Operation(
        summary = "Step 3: Connect with Food Lovers",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Finalizes the onboarding by following suggested users. Completes the onboarding process and activates the user account."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Onboarding completed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied: User is not in STEP_3")
    })
    @PostMapping("/connections")
    @RequiresOnboardingStep(OnboardingStatus.STEP_3)
    public ResponseEntity<OnboardingProgressDto> establishConnections(
        @RequestBody OnboardingConnectionsRequest connections,
        @RequestParam(value = "shouldSkip", defaultValue = "false") boolean shouldSkip
    ) {
        return this.onboardingService.followInitialUsers(connections, shouldSkip);
    }

    @Operation(
        summary = "Go back to the previous onboarding step",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Reverts the user's onboarding status to the immediate previous stage. " +
            "This is not allowed if the user is already at the first step or has already finished the process."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully reverted to the previous step"),
        @ApiResponse(responseCode = "400", description = "Cannot go back: User is already at the initial onboarding step."),
        @ApiResponse(responseCode = "401", description = "Account not verified. Please check your email to proceed."),
        @ApiResponse(responseCode = "403", description = "Onboarding has already been completed. Status cannot be reverted.")
    })
    @PatchMapping("/back")
    public ResponseEntity<OnboardingProgressDto> backToPreviousStep() throws BadRequestException {
        return this.onboardingService.backToPreviousStep();
    }

    @Operation(
        summary = "Get current onboarding step",
        description = "Retrieves the current onboarding status for the authenticated user. " +
                "This helps the frontend determine which screen to display next.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = OnboardingProgressDto.class)),
            description = "Current step retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User must be logged in"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/step")
    public ResponseEntity<OnboardingProgressDto> getCurrentStep() {
        OnboardingProgressDto response = this.onboardingService.getCurrentStep();
        return ResponseEntity.ok(response);
    }
}

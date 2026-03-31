package com.rodrigo.tastyhub.modules.user.application.dto.response;

import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed progress of the user's onboarding journey")
public record OnboardingProgressDto(
    @Schema(description = "The current state the user is in", example = "STEP_1")
    OnboardingStatus currentStatus,

    @Schema(description = "The suggested next action/step for the user", example = "STEP_2")
    OnboardingStatus nextStepAction,

    @Schema(description = "Flag indicating if the user has finished all onboarding requirements", example = "false")
    boolean isCompleted
) {}

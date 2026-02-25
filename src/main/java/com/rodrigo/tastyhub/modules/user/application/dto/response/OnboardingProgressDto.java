package com.rodrigo.tastyhub.modules.user.application.dto.response;

import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;

public record OnboardingProgressDto(
    OnboardingStatus currentStatus,
    OnboardingStatus nextStepAction,
    boolean isCompleted
) {}

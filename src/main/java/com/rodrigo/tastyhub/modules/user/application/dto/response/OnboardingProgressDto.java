package com.rodrigo.tastyhub.modules.user.application.dto.response;

import com.rodrigo.tastyhub.modules.user.domain.model.OnBoardingStatus;

public record OnboardingProgressDto(
    OnBoardingStatus currentStatus,
    OnBoardingStatus nextStepAction,
    boolean isCompleted
) {}

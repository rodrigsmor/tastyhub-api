package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingBackPreviousStepUseCase {
    private final SecurityService securityService;
    private final OnboardingService onboardingService;

    @Transactional
    public OnboardingProgressDto execute() {
        var user = securityService.getCurrentUser();

        user = onboardingService.backToPreviousStep(user.getId());

        return new OnboardingProgressDto(
            user.getOnboardingStatus(),
            user.getOnboardingStatus().getNext(),
            user.isOnboardingFinished()
        );
    }
}

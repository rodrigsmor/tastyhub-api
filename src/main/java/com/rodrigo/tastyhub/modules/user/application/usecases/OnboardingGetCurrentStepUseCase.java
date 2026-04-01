package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingGetCurrentStepUseCase {
    private final SecurityService securityService;

    @Transactional
    public OnboardingProgressDto execute() {
        User user = securityService.getCurrentUser();

        OnboardingStatus status = user.getOnboardingStatus();

        return new OnboardingProgressDto(
            status,
            status.getNext(),
            user.isOnboardingFinished()
        );
    }
}

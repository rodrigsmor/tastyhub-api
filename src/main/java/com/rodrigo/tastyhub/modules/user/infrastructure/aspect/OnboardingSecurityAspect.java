package com.rodrigo.tastyhub.modules.user.infrastructure.aspect;

import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresOnboardingStep;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OnboardingSecurityAspect {
    private final SecurityService securityService;

    @Before("@annotation(onboardingAnnotation)")
    public void validateOnboardingStep(RequiresOnboardingStep onboardingAnnotation) {
        User user = securityService.getCurrentUser();

        var requiredStatus = onboardingAnnotation.value();

        if (user.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new ForbiddenException("Onboarding already completed.");
        }

        if (user.getOnboardingStatus() != requiredStatus) {
            throw new ForbiddenException(
                "Access denied: User is not in " + requiredStatus
            );
        }
    }
}

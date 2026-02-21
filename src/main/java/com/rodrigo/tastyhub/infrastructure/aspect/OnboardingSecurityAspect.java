package com.rodrigo.tastyhub.infrastructure.aspect;

import com.rodrigo.tastyhub.domain.annotations.RequiresOnboardingStep;
import com.rodrigo.tastyhub.domain.model.OnBoardingStatus;
import com.rodrigo.tastyhub.domain.model.User;
import com.rodrigo.tastyhub.exceptions.OnboardingException;
import com.rodrigo.tastyhub.infrastructure.security.SecurityService;
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

        if (user.getOnBoardingStatus() == OnBoardingStatus.COMPLETED) {
            throw new OnboardingException("Onboarding already completed.");
        }

        if (user.getOnBoardingStatus() != requiredStatus) {
            throw new OnboardingException(
                "Access denied: User is not in " + requiredStatus
            );
        }
    }
}

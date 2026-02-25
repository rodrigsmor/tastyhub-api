package com.rodrigo.tastyhub.modules.user.infrastructure.aspect;

import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class VerificationAspect {
    private final SecurityService securityService;

    @Before("@annotation(requiresVerification)")
    public void checkUserVerification(RequiresVerification requiresVerification) {
        User user = securityService.getCurrentUser();

        if (!user.isVerified()) {
            throw new ForbiddenException("Account not verified. Please check your email.");
        }
    }
}

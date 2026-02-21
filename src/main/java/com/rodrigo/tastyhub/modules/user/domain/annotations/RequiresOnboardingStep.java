package com.rodrigo.tastyhub.modules.user.domain.annotations;

import com.rodrigo.tastyhub.modules.user.domain.model.OnBoardingStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresOnboardingStep {
    OnBoardingStatus value();
}

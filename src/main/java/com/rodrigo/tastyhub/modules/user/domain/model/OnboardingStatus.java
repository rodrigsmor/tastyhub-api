package com.rodrigo.tastyhub.modules.user.domain.model;

public enum OnboardingStatus {
    PENDING_VERIFICATION("User needs to verify email"),
    STEP_1("First step of onboarding: Profile details"),
    STEP_2("Second step of onboarding: Preferences"),
    STEP_3("Third step of onboarding: Final setup"),
    COMPLETED("Onboarding finished");

    private final String description;

    OnboardingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInProgress() {
        return this != PENDING_VERIFICATION && this != COMPLETED;
    }

    public OnboardingStatus getNext() {
        int nextIndex = this.ordinal() + 1;
        OnboardingStatus[] values = OnboardingStatus.values();

        if (nextIndex >= values.length) {
            return this;
        }

        return values[nextIndex];
    }
}

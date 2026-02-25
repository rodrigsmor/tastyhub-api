package com.rodrigo.tastyhub.modules.user.domain.model;

public enum OnboardingStatus {
    PENDING_VERIFICATION,
    STEP_1,
    STEP_2,
    STEP_3,
    COMPLETED;

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
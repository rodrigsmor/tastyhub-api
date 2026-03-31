package com.rodrigo.tastyhub.modules.user.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the possible states of a user during the onboarding process")
public enum OnboardingStatus {
    @Schema(description = "User has registered but hasn't verified their email yet")
    PENDING_VERIFICATION,

    @Schema(description = "Basic profile information (username, bio) needs to be filled")
    STEP_1,

    @Schema(description = "Preferences or additional details (categories, interests) pending")
    STEP_2,

    @Schema(description = "Final configuration or tutorial completion")
    STEP_3,

    @Schema(description = "Onboarding process finished successfully")
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
package com.rodrigo.tastyhub.domain.model;

public enum UserStatus {
    PENDING("Account created but not verified"),
    ACTIVE("Account verified and active"),
    DISABLED("Account deactivated by user or admin");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

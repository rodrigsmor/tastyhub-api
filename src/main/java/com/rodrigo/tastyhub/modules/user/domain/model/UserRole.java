package com.rodrigo.tastyhub.modules.user.domain.model;

public enum UserRole {

    ROLE_USER,
    ROLE_ADMIN;

    public String authority() {
        return name();
    }
}


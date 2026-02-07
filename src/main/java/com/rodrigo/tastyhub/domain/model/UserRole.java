package com.rodrigo.tastyhub.domain.model;

public enum UserRole {

    ROLE_USER,
    ROLE_ADMIN;

    public String authority() {
        return name();
    }
}


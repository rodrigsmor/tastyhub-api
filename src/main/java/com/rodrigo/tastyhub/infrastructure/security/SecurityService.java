package com.rodrigo.tastyhub.infrastructure.security;

import com.rodrigo.tastyhub.domain.model.User;
import com.rodrigo.tastyhub.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        return (User) auth.getPrincipal();
    }
}

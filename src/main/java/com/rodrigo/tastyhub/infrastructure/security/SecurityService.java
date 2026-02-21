package com.rodrigo.tastyhub.infrastructure.security;

import com.rodrigo.tastyhub.domain.model.User;
import com.rodrigo.tastyhub.domain.repository.UserRepository;
import com.rodrigo.tastyhub.exceptions.ResourceNotFoundException;
import com.rodrigo.tastyhub.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.user.domain.model.Role;
import com.rodrigo.tastyhub.modules.user.domain.model.UserRole;
import com.rodrigo.tastyhub.modules.user.domain.repository.RoleRepository;
import com.rodrigo.tastyhub.shared.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getDefaultRole() {
        return roleRepository.findByName(UserRole.ROLE_USER)
            .orElseThrow(() -> new InfrastructureException("Critical Error: Default Role not found in database!"));
    }
}

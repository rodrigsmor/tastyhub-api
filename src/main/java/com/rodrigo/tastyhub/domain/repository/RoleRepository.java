package com.rodrigo.tastyhub.domain.repository;

import com.rodrigo.tastyhub.domain.model.Role;
import com.rodrigo.tastyhub.domain.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(UserRole name);
}

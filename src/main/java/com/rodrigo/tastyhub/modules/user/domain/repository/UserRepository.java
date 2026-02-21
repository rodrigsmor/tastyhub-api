package com.rodrigo.tastyhub.modules.user.domain.repository;

import com.rodrigo.tastyhub.modules.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Boolean existsByIdIn(Collection<Long> id);
}

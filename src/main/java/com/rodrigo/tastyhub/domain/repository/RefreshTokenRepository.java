package com.rodrigo.tastyhub.domain.repository;

import com.rodrigo.tastyhub.domain.model.RefreshToken;
import com.rodrigo.tastyhub.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);
}
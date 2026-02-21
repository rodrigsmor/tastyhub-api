package com.rodrigo.tastyhub.modules.auth.domain.repository;

import com.rodrigo.tastyhub.modules.auth.domain.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByToken(String token);
}

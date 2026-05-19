package com.digitalcow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Este repositorio consulta y guarda los tokens de verificacion de email. */
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
}

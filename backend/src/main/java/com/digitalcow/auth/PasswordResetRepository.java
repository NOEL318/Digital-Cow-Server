package com.digitalcow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Este repositorio consulta y guarda los tokens de reset de contrasena. */
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByToken(String token);
}

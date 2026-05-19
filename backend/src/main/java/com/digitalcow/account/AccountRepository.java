package com.digitalcow.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Acceso a Account. No filtrado por tenant (Account ES el tenant). */
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findBySlug(String slug);
    boolean existsBySlug(String slug);
}

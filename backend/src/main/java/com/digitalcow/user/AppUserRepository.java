package com.digitalcow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Acceso a AppUser. Algunos metodos cruzan tenant (login). */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findAllByAccountId(Long accountId);
    boolean existsByRole(UserRole role);
}

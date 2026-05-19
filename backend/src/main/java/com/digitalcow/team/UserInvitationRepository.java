package com.digitalcow.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Este repositorio consulta y guarda las invitaciones de usuario. */
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByToken(String token);
    List<UserInvitation> findAllByAcceptedAtIsNull();
}

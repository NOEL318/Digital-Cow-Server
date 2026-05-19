package com.digitalcow.team;

import com.digitalcow.team.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de gestion de equipo del tenant. */
@RestController
@RequestMapping("/api/v1/team")
public class TeamController {

    private final TeamService svc;

    public TeamController(TeamService svc) { this.svc = svc; }

    /** Este metodo lista los usuarios. */
    @GetMapping
    public List<TeamUserDto> listUsers() { return svc.listUsers(); }

    /** Este metodo lista las invitaciones. */
    @GetMapping("/invitations")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public List<InvitationDto> listInvitations() { return svc.listPendingInvitations(); }

    /** Este metodo crea una invitacion para un nuevo miembro del equipo. */
    @PostMapping("/invitations")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public InvitationDto invite(@Valid @RequestBody InviteUserRequest req) { return svc.invite(req); }

    /** Este metodo elimina la invitacion. */
    @DeleteMapping("/invitations/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<Void> deleteInvitation(@PathVariable Long id) {
        svc.deleteInvitation(id);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo acepta una invitacion pendiente y crea el usuario asociado. */
    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<Void> accept(@PathVariable String token,
                                       @Valid @RequestBody AcceptInvitationRequest req) {
        svc.accept(token, req);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo actualiza el usuario. */
    @PatchMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public TeamUserDto updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return svc.updateUser(id, req);
    }
}

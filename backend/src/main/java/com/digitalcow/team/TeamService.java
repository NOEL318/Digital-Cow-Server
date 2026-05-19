package com.digitalcow.team;

import com.digitalcow.audit.Auditable;
import com.digitalcow.audit.AuditLog;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.mail.EmailSender;
import com.digitalcow.team.dto.*;
import com.digitalcow.tenancy.SkipTenancy;
import com.digitalcow.tenancy.TenantContext;
import com.digitalcow.user.AppUser;
import com.digitalcow.user.AppUserRepository;
import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Gestion de equipos: listado, invitaciones, acept., cambio de rol. */
@Service
public class TeamService {

    private static final Duration INV_TTL = Duration.ofDays(7);

    private final AppUserRepository users;
    private final UserInvitationRepository invitations;
    private final PasswordEncoder encoder;
    private final EmailSender mail;

    public TeamService(AppUserRepository users, UserInvitationRepository invitations,
                       PasswordEncoder encoder, EmailSender mail) {
        this.users = users;
        this.invitations = invitations;
        this.encoder = encoder;
        this.mail = mail;
    }

    /** Este metodo lista los usuarios. */
    public List<TeamUserDto> listUsers() {
        Long accId = TenantContext.get();
        return users.findAllByAccountId(accId).stream()
            .map(u -> new TeamUserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus()))
            .toList();
    }

    /** Este metodo lista el equipo pending las invitaciones. */
    public List<InvitationDto> listPendingInvitations() {
        return invitations.findAllByAcceptedAtIsNull().stream()
            .map(i -> new InvitationDto(i.getId(), i.getEmail(), i.getRole(), i.getExpiresAt(), i.getAcceptedAt()))
            .toList();
    }

    /** Este metodo invita el equipo. */
    @Transactional
    @Auditable(entityType = "Invitation", action = AuditLog.Action.INVITE)
    public InvitationDto invite(InviteUserRequest req) {
        var p = CurrentUser.require();
        UserInvitation inv = new UserInvitation();
        inv.setAccountId(p.accountId());
        inv.setEmail(req.email().toLowerCase());
        inv.setRole(req.role());
        inv.setToken(UUID.randomUUID().toString().replace("-", ""));
        inv.setExpiresAt(Instant.now().plus(INV_TTL));
        inv.setCreatedByUserId(p.userId());
        invitations.save(inv);
        mail.send(inv.getEmail(), "You have been invited to Digital Cow",
            "<p>Accept token: <code>" + inv.getToken() + "</code></p>");
        return new InvitationDto(inv.getId(), inv.getEmail(), inv.getRole(), inv.getExpiresAt(), null);
    }

    /** Este metodo elimina la invitacion. */
    @Transactional
    public void deleteInvitation(Long id) {
        invitations.deleteById(id);
    }

    /** Acepta invitacion: NO requiere auth previa (skip tenancy). */
    @Transactional
    @SkipTenancy
    @Auditable(entityType = "User", action = AuditLog.Action.CREATE)
    public void accept(String token, AcceptInvitationRequest req) {
        UserInvitation inv = invitations.findByToken(token)
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.INVITATION_INVALID, "Invalid token"));
        if (inv.getAcceptedAt() != null) {
            throw BusinessException.conflict(ErrorCode.INVITATION_ALREADY_ACCEPTED, "Already accepted");
        }
        if (inv.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.INVITATION_EXPIRED, "Expired");
        }
        if (users.existsByEmail(inv.getEmail())) {
            throw BusinessException.conflict(ErrorCode.AUTH_EMAIL_ALREADY_USED, "Email already used");
        }
        AppUser u = new AppUser();
        u.setAccountId(inv.getAccountId());
        u.setEmail(inv.getEmail());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(inv.getRole());
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerifiedAt(Instant.now());
        users.save(u);
        inv.setAcceptedAt(Instant.now());
    }

    /** Este metodo actualiza el usuario. */
    @Transactional
    public TeamUserDto updateUser(Long userId, UpdateUserRequest req) {
        AppUser u = users.findById(userId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "User not found"));
        if (!u.getAccountId().equals(TenantContext.get())) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-tenant");
        }
        if (req.role() != null && req.role() != UserRole.SUPERADMIN) u.setRole(req.role());
        if (req.status() != null) u.setStatus(req.status());
        return new TeamUserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus());
    }
}

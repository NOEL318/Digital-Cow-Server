package com.digitalcow.auth;

import com.digitalcow.account.*;
import com.digitalcow.audit.Auditable;
import com.digitalcow.audit.AuditLog;
import com.digitalcow.auth.dto.*;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.mail.EmailSender;
import com.digitalcow.tenancy.SkipTenancy;
import com.digitalcow.user.AppUser;
import com.digitalcow.user.AppUserRepository;
import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio que maneja registro, login, refresh, logout, verificacion de email,
 * reset de password. Marcado SkipTenancy: opera sin tenant en contexto.
 */
@Service
@SkipTenancy
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Duration PWD_RESET_TTL = Duration.ofHours(2);

    private final AccountRepository accounts;
    private final AppUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final EmailVerificationRepository verifications;
    private final PasswordResetRepository resets;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final EmailSender mail;
    private final AccountSeeding accountSeeding;

    public AuthService(AccountRepository accounts, AppUserRepository users,
                       RefreshTokenRepository refreshTokens, EmailVerificationRepository verifications,
                       PasswordResetRepository resets, PasswordEncoder passwordEncoder,
                       JwtService jwt, EmailSender mail, AccountSeeding accountSeeding) {
        this.accounts = accounts;
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.verifications = verifications;
        this.resets = resets;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.mail = mail;
        this.accountSeeding = accountSeeding;
    }

    /** Crea cuenta + usuario Owner + dispara verificacion de email. */
    @Transactional
    @Auditable(entityType = "User", action = AuditLog.Action.CREATE)
    public AuthTokensResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw BusinessException.conflict(ErrorCode.AUTH_EMAIL_ALREADY_USED, "Email already used");
        }
        Account acc = new Account();
        acc.setName(req.accountName());
        acc.setSlug(slugify(req.accountName()));
        acc.setDefaultLocale(req.locale() != null ? req.locale() : Locale.es);
        acc = accounts.save(acc);

        // Categorias por defecto editables por el tenant.
        accountSeeding.seedDefaultCategories(acc.getId());

        AppUser u = new AppUser();
        u.setAccountId(acc.getId());
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(UserRole.OWNER);
        u.setLocale(req.locale());
        u.setStatus(UserStatus.ACTIVE);
        // La cuenta queda verificada al instante: el registro ya no exige confirmar el correo
        // ni envia un token por email. El usuario entra directo tras crear la cuenta.
        u.setEmailVerifiedAt(Instant.now());
        u = users.save(u);

        return issueTokens(u);
    }

    /** Login con email + password. Rechaza si DISABLED o no verificado. */
    @Transactional
    @Auditable(entityType = "User", action = AuditLog.Action.LOGIN)
    public AuthTokensResponse login(LoginRequest req) {
        AppUser u = users.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw BusinessException.unauthorized(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials");
        }
        if (u.getStatus() == UserStatus.DISABLED) {
            throw BusinessException.forbidden(ErrorCode.AUTH_USER_DISABLED, "User disabled");
        }
        // Ya no se exige email verificado para iniciar sesion: el registro auto-verifica la cuenta.
        return issueTokens(u);
    }

    /** Rota el refresh token (uno valido a la vez). */
    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        String hash = TokenHasher.sha256Hex(req.refreshToken());
        RefreshToken rt = refreshTokens.findByTokenHash(hash)
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "Invalid refresh"));
        if (rt.getRevokedAt() != null || rt.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "Expired refresh");
        }
        rt.setRevokedAt(Instant.now());
        AppUser u = users.findById(rt.getUserId())
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "User missing"));
        return issueTokens(u);
    }

    /** Revoca el refresh token actual. */
    @Transactional
    public void logout(String refreshToken) {
        String hash = TokenHasher.sha256Hex(refreshToken);
        refreshTokens.findByTokenHash(hash).ifPresent(rt -> rt.setRevokedAt(Instant.now()));
    }

    /** Marca email verificado consumiendo token. */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerification v = verifications.findByToken(token)
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "Invalid token"));
        if (v.getUsedAt() != null || v.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_TOKEN_EXPIRED, "Expired or used");
        }
        AppUser u = users.findById(v.getUserId())
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "User missing"));
        // Solo se actualiza el sello de verificacion si el usuario no estaba ya verificado,
        // para evitar que se reescriba la fecha si alguien reutiliza un token viejo.
        if (u.getEmailVerifiedAt() == null) {
            u.setEmailVerifiedAt(Instant.now());
        }
        v.setUsedAt(Instant.now());
    }

    /** Genera token de reset (silencioso si email no existe). */
    @Transactional
    public void requestPasswordReset(RequestPasswordResetRequest req) {
        users.findByEmail(req.email().toLowerCase()).ifPresent(u -> {
            PasswordReset r = new PasswordReset();
            r.setUserId(u.getId());
            r.setToken(UUID.randomUUID().toString().replace("-", ""));
            r.setExpiresAt(Instant.now().plus(PWD_RESET_TTL));
            resets.save(r);
            try {
                mail.send(u.getEmail(), "Reset your Digital Cow password",
                    "<p>Reset token: <code>" + r.getToken() + "</code></p>");
            } catch (Exception e) {
                // Nunca loguear el token en texto plano. El usuario podra solicitar otro reset
                // si no recibe el correo; loguear solo el identificador del usuario.
                log.warn("Password reset email send failed for userId={}: {}",
                    u.getId(), e.getMessage());
            }
        });
    }

    /** Aplica nueva password tras validar token. */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordReset r = resets.findByToken(req.token())
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "Invalid token"));
        if (r.getUsedAt() != null || r.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_TOKEN_EXPIRED, "Expired or used");
        }
        AppUser u = users.findById(r.getUserId())
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "User missing"));
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        r.setUsedAt(Instant.now());
        refreshTokens.deleteAllByUserId(u.getId());
    }

    private AuthTokensResponse issueTokens(AppUser u) {
        String access = jwt.issueAccess(u.getId(), u.getAccountId(), u.getEmail(), List.of(u.getRole()));
        String refresh = jwt.issueRefresh();
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(TokenHasher.sha256Hex(refresh));
        rt.setExpiresAt(Instant.now().plus(jwt.refreshTtl()));
        refreshTokens.save(rt);
        return new AuthTokensResponse(access, refresh, jwt.accessTtl().toSeconds());
    }

    private String slugify(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (base.isEmpty()) base = "account";
        String slug = base;
        int n = 1;
        while (accounts.existsBySlug(slug)) { slug = base + "-" + n++; }
        return slug;
    }
}

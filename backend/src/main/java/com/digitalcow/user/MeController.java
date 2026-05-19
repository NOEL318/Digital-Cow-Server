package com.digitalcow.user;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.user.dto.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/** Endpoints del usuario autenticado (perfil propio). */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public MeController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** Este metodo devuelve el usuario actual. */
    @GetMapping
    public MeResponse get() {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        return new MeResponse(u.getId(), u.getAccountId(), u.getEmail(), u.getFullName(),
            u.getRole(), u.getLocale(), u.getEmailVerifiedAt() != null);
    }

    /** Este metodo actualiza el usuario actual. */
    @PatchMapping
    @Transactional
    public MeResponse update(@Valid @RequestBody UpdateMeRequest req) {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.locale() != null) u.setLocale(req.locale());
        return new MeResponse(u.getId(), u.getAccountId(), u.getEmail(), u.getFullName(),
            u.getRole(), u.getLocale(), u.getEmailVerifiedAt() != null);
    }

    /** Este metodo actualiza la contrasena. */
    @PatchMapping("/password")
    @Transactional
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest req) {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_INVALID_CREDENTIALS, "Bad password");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
        return ResponseEntity.noContent().build();
    }
}

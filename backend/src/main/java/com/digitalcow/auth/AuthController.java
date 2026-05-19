package com.digitalcow.auth;

import com.digitalcow.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints publicos de autenticacion (sin tenant). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService svc;

    public AuthController(AuthService svc) { this.svc = svc; }

    /** Registra una cuenta nueva y devuelve los tokens iniciales. */
    @PostMapping("/register")
    public AuthTokensResponse register(@Valid @RequestBody RegisterRequest req) {
        return svc.register(req);
    }

    /** Inicia sesion con email y contrasena y devuelve los tokens. */
    @PostMapping("/login")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest req) {
        return svc.login(req);
    }

    /** Renueva el token de acceso usando el refresh token. */
    @PostMapping("/refresh")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return svc.refresh(req);
    }

    /** Cierra la sesion invalidando el refresh token recibido. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        svc.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /** Confirma el email del usuario usando el token enviado por correo. */
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        svc.verifyEmail(req.token());
        return ResponseEntity.noContent().build();
    }

    /** Solicita un correo con instrucciones para restablecer la contrasena. */
    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody RequestPasswordResetRequest req) {
        svc.requestPasswordReset(req);
        return ResponseEntity.noContent().build();
    }

    /** Aplica la nueva contrasena validando el token de reset. */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        svc.resetPassword(req);
        return ResponseEntity.noContent().build();
    }
}

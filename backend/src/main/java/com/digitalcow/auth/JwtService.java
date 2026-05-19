package com.digitalcow.auth;

import com.digitalcow.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Emite y valida JWT HS256. Refresh es opaco (UUID), no JWT.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
        @Value("${digitalcow.security.jwt.secret}") String secret,
        @Value("${digitalcow.security.jwt.access-token-ttl-minutes}") long accessMinutes,
        @Value("${digitalcow.security.jwt.refresh-token-ttl-days}") long refreshDays
    ) {
        // Esta validacion asegura al arrancar que el secret tiene la longitud minima
        // que exige HS256, en lugar de fallar mas tarde al emitir el primer token.
        byte[] secretBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET debe tener al menos 32 bytes. Generar con: openssl rand -base64 48");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    /** Devuelve la duracion del access token configurada en application.yml. */
    public Duration accessTtl() { return accessTtl; }

    /** Genera access token JWT con userId, accountId (nullable), email y roles. */
    public String issueAccess(Long userId, Long accountId, String email, List<UserRole> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("accountId", accountId)
            .claim("email", email)
            .claim("roles", roles.stream().map(Enum::name).toList())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTtl)))
            .signWith(key)
            .compact();
    }

    /** Genera un refresh opaco (UUID v4). */
    public String issueRefresh() {
        return UUID.randomUUID().toString();
    }

    /** Parsea y valida un JWT. Lanza si invalido o expirado. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /** Este metodo recarga el JWT ttl. */
    public Duration refreshTtl() { return refreshTtl; }
}

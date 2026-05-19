package com.digitalcow.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Token de verificacion de email. Un solo uso, expira. */
@Entity
@Table(name = "email_verification")
@Getter @Setter @NoArgsConstructor
public class EmailVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;
}

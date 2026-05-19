package com.digitalcow.user;

import com.digitalcow.account.Locale;
import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Usuario del sistema. SUPERADMIN tiene account_id NULL. */
@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor
public class AppUser extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NULL solo para SUPERADMIN. */
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 2)
    private Locale locale;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private UserStatus status = UserStatus.INVITED;
}

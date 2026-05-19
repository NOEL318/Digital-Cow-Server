package com.digitalcow.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Registro inmutable de operacion sensible. */
@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    public enum Action { CREATE, UPDATE, DELETE, LOGIN, INVITE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Action action;

    @Column(name = "payload_json", columnDefinition = "JSON")
    private String payloadJson;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 250)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}

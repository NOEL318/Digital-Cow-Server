package com.digitalcow.account;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant root. Cada Account agrupa todos los datos de una organizacion. */
@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Account extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private AccountPlan plan = AccountPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_locale", nullable = false, length = 2)
    private Locale defaultLocale = Locale.es;
}

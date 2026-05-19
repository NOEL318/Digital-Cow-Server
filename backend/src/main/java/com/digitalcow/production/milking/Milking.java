package com.digitalcow.production.milking;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro diario de ordeno por animal y sesion. UQ por (account_id, animal_id, milking_date, session).
 */
@Entity
@Table(name = "milking")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Milking extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "milking_date", nullable = false)
    private LocalDate milkingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private MilkingSession session = MilkingSession.TOTAL;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal liters;

    @Column(name = "recorded_by_user_id")
    private Long recordedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

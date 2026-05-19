package com.digitalcow.health.pestcontrol;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * Aplicacion de control de plagas a un rancho o lote.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "pest_control")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class PestControl extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id")
    private Long ranchId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "pest_id", nullable = false)
    private Long pestId;

    @Column(name = "product_used", nullable = false, length = 200)
    private String productUsed;

    @Column(length = 120)
    private String dose;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @Column(name = "next_application_at")
    private LocalDate nextApplicationAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "applied_by_user_id")
    private Long appliedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

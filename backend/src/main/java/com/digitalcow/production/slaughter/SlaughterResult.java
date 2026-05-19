package com.digitalcow.production.slaughter;

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

/** Resultado de sacrificio: pesos vivo y canal, rendimiento, grado, comprador. */
@Entity
@Table(name = "slaughter_result")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class SlaughterResult extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "slaughtered_at", nullable = false)
    private LocalDate slaughteredAt;

    @Column(name = "live_weight_kg", precision = 7, scale = 2)
    private BigDecimal liveWeightKg;

    @Column(name = "carcass_weight_kg", precision = 7, scale = 2)
    private BigDecimal carcassWeightKg;

    @Column(name = "yield_pct", precision = 5, scale = 2)
    private BigDecimal yieldPct;

    @Column(length = 40)
    private String grade;

    @Column(length = 160)
    private String buyer;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}

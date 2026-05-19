package com.digitalcow.health.treatment;

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
 * Tratamiento medico aplicado a un animal. Multi-tenant.
 * Las fechas de retiro de leche y carne se calculan en el service.
 */
@Entity
@Table(name = "treatment")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Treatment extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @Column(name = "medication_id", nullable = false)
    private Long medicationId;

    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt;

    @Column(length = 120)
    private String dose;

    @Column(name = "doses_count")
    private Short dosesCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TreatmentRoute route;

    @Column(name = "withdrawal_milk_until")
    private LocalDate withdrawalMilkUntil;

    @Column(name = "withdrawal_meat_until")
    private LocalDate withdrawalMeatUntil;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "prescribed_by", length = 160)
    private String prescribedBy;

    @Column(name = "vet_visit_id")
    private Long vetVisitId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

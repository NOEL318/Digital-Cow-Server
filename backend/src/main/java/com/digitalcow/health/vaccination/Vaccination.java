package com.digitalcow.health.vaccination;

import com.digitalcow.catalog.vaccine.VaccineRoute;
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
 * Aplicacion de vacuna a un animal individual o en lote.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "vaccination")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Vaccination extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "vaccine_id", nullable = false)
    private Long vaccineId;

    @Column(name = "batch_number", length = 80)
    private String batchNumber;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @Column(name = "dose_ml", precision = 5, scale = 2)
    private BigDecimal doseMl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VaccineRoute route;

    @Column(name = "next_dose_due")
    private LocalDate nextDoseDue;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "applied_by_user_id")
    private Long appliedByUserId;

    @Column(name = "vet_visit_id")
    private Long vetVisitId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

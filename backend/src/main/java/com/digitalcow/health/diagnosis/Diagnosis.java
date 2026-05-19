package com.digitalcow.health.diagnosis;

import com.digitalcow.catalog.disease.DiseaseSeverity;
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

import java.time.LocalDate;

/**
 * Diagnostico clinico de un animal. Siempre individual.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "diagnosis")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Diagnosis extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "disease_id", nullable = false)
    private Long diseaseId;

    @Column(name = "diagnosed_at", nullable = false)
    private LocalDate diagnosedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private DiseaseSeverity severity = DiseaseSeverity.MEDIUM;

    @Column(length = 500)
    private String symptoms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DiagnosisStatus status = DiagnosisStatus.ACTIVE;

    @Column(name = "resolved_at")
    private LocalDate resolvedAt;

    @Column(name = "diagnosed_by_user_id")
    private Long diagnosedByUserId;

    @Column(name = "vet_visit_id")
    private Long vetVisitId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

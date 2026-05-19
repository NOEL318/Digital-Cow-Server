package com.digitalcow.reproduction.pregnancy;

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
 * Diagnostico de gestacion sobre una hembra.
 * Si el resultado es POSITIVE y hay estimatedGestationDays se calcula estimatedCalvingDate.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "pregnancy_check")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class PregnancyCheck extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "checked_at", nullable = false)
    private LocalDate checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private PregnancyMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PregnancyResult result;

    @Column(name = "estimated_gestation_days")
    private Short estimatedGestationDays;

    @Column(name = "estimated_calving_date")
    private LocalDate estimatedCalvingDate;

    @Column(name = "vet_visit_id")
    private Long vetVisitId;

    @Column(name = "checked_by_user_id")
    private Long checkedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

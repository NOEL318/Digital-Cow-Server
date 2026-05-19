package com.digitalcow.reproduction.calving;

import com.digitalcow.animal.Sex;
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
 * Parto de una hembra. Puede enlazarse a un Animal hijo registrado en la cuenta.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "calving")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Calving extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "calved_at", nullable = false)
    private LocalDate calvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private CalvingEase ease = CalvingEase.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CalvingOutcome outcome = CalvingOutcome.LIVE;

    @Column(name = "calf_animal_id")
    private Long calfAnimalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calf_sex", length = 8)
    private Sex calfSex;

    @Column(name = "calf_birth_weight_kg", precision = 5, scale = 2)
    private BigDecimal calfBirthWeightKg;

    @Column(name = "pregnancy_check_id")
    private Long pregnancyCheckId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}

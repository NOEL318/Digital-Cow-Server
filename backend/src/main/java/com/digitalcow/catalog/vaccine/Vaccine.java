package com.digitalcow.catalog.vaccine;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Catalogo global de vacunas bovinas. Sin account_id (datos seed compartidos).
 */
@Entity
@Table(name = "vaccine")
@Getter
@Setter
public class Vaccine extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "target_diseases", length = 400)
    private String targetDiseases;

    @Column(name = "default_dose_ml", precision = 5, scale = 2)
    private BigDecimal defaultDoseMl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VaccineRoute route;

    @Column(name = "recommended_age_months")
    private Short recommendedAgeMonths;

    @Column(name = "recommended_frequency_months")
    private Short recommendedFrequencyMonths;

    @Column(nullable = false, length = 20)
    private String species = "BOVINE";
}

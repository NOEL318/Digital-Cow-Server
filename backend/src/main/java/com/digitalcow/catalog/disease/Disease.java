package com.digitalcow.catalog.disease;

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

/**
 * Catalogo global de enfermedades. Sin account_id.
 */
@Entity
@Table(name = "disease")
@Getter
@Setter
public class Disease extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DiseaseCategory category;

    @Column(nullable = false)
    private boolean zoonotic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private DiseaseSeverity severity = DiseaseSeverity.MEDIUM;

    @Column(name = "default_symptoms", length = 500)
    private String defaultSymptoms;
}

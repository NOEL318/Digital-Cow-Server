package com.digitalcow.reproduction.heat;

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

import java.time.Instant;

/**
 * Deteccion de celo de una hembra. detected_at es timestamp porque captura momento del dia.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "heat")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Heat extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_method", length = 20)
    private DetectionMethod detectionMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private HeatIntensity intensity;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "detected_by_user_id")
    private Long detectedByUserId;
}

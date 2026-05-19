package com.digitalcow.catalog.medication;

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

/**
 * Catalogo de medicamentos. Los registros con account_id NULL son
 * seeds globales compartidos por todos los tenants. Los registros con
 * account_id no nulo pertenecen al tenant que los creo (marca comercial,
 * presentacion concreta, codigo de barras).
 */
@Entity
@Table(name = "medication")
@EntityListeners(TenantAwareEntityListener.class)
@Getter
@Setter
public class Medication extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null = seed global; no nulo = propiedad del tenant indicado. */
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "active_ingredient", length = 200)
    private String activeIngredient;

    @Column(length = 160)
    private String manufacturer;

    @Column(length = 160)
    private String presentation;

    @Column(length = 40)
    private String barcode;

    /** Fecha de caducidad del envase actual. */
    @Column(name = "expires_at")
    private java.time.LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicationCategory category = MedicationCategory.OTHER;

    @Column(name = "default_dose", length = 120)
    private String defaultDose;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_route", length = 20)
    private MedicationRoute defaultRoute;

    @Column(name = "withdrawal_milk_days", nullable = false)
    private short withdrawalMilkDays;

    @Column(name = "withdrawal_meat_days", nullable = false)
    private short withdrawalMeatDays;

    @Column(length = 400)
    private String notes;
}

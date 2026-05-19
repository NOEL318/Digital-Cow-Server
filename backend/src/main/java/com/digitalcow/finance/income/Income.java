package com.digitalcow.finance.income;

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

/** Ingreso del tenant. Puede ser manual o generado por venta automatica. */
@Entity
@Table(name = "income")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Income extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "income_category_id", nullable = false)
    private Long incomeCategoryId;

    @Column(name = "received_at", nullable = false)
    private LocalDate receivedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(name = "ranch_id")
    private Long ranchId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(length = 400)
    private String description;

    @Column(length = 160)
    private String payer;

    @Column(name = "invoice_number", length = 80)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 16)
    private IncomeSourceType sourceType = IncomeSourceType.MANUAL;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}

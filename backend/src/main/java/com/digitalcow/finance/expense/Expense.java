package com.digitalcow.finance.expense;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Gasto manual del tenant, opcionalmente ligado a un rancho/lote/animal. */
@Entity
@Table(name = "expense")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Expense extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "expense_category_id", nullable = false)
    private Long expenseCategoryId;

    @Column(name = "incurred_at", nullable = false)
    private LocalDate incurredAt;

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
    private String vendor;

    @Column(name = "invoice_number", length = 80)
    private String invoiceNumber;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}

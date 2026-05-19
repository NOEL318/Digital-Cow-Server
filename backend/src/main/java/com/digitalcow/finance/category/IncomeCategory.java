package com.digitalcow.finance.category;

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

/**
 * Categoria de ingreso. account_id nullable: NULL = catalogo global del sistema.
 * El filtro multi-tenant permite ver los globales y los del tenant.
 */
@Entity
@Table(name = "income_category")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId OR account_id IS NULL")
@Getter
@Setter
public class IncomeCategory extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IncomeKind kind;

    @Column(length = 400)
    private String notes;
}

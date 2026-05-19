package com.digitalcow.feeding.feeditem;

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

/**
 * Insumo alimenticio. account_id nullable: NULL = catalogo global del sistema.
 * El filtro multi-tenant permite ver los globales y los del tenant.
 */
@Entity
@Table(name = "feed_item")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId OR account_id IS NULL")
@Getter
@Setter
public class FeedItem extends AbstractAuditableEntity {

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
    private FeedCategory category;

    @Column(name = "dry_matter_pct", precision = 5, scale = 2)
    private BigDecimal dryMatterPct;

    @Column(name = "protein_pct", precision = 5, scale = 2)
    private BigDecimal proteinPct;

    @Column(name = "energy_mcal_kg", precision = 5, scale = 2)
    private BigDecimal energyMcalKg;

    @Column(name = "unit_cost", precision = 10, scale = 4)
    private BigDecimal unitCost;

    @Column(length = 3)
    private String currency;

    @Column(length = 400)
    private String notes;
}

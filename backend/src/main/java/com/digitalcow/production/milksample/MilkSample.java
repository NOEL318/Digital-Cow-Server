package com.digitalcow.production.milksample;

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

/** Resultado de muestra de leche (laboratorio o medidor). Multi-tenant. */
@Entity
@Table(name = "milk_sample")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class MilkSample extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "sampled_at", nullable = false)
    private LocalDate sampledAt;

    @Column(name = "scc_cells_per_ml")
    private Integer sccCellsPerMl;

    @Column(name = "fat_pct", precision = 4, scale = 2)
    private BigDecimal fatPct;

    @Column(name = "protein_pct", precision = 4, scale = 2)
    private BigDecimal proteinPct;

    @Column(name = "lactose_pct", precision = 4, scale = 2)
    private BigDecimal lactosePct;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

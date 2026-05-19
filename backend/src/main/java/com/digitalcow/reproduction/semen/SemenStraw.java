package com.digitalcow.reproduction.semen;

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

/**
 * Pajilla de semen en inventario. Asociada a un toro.
 * available_quantity se decrementa al crear un servicio AI.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "semen_straw")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class SemenStraw extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "bull_id", nullable = false)
    private Long bullId;

    @Column(length = 160)
    private String provider;

    @Column(name = "batch_number", length = 80)
    private String batchNumber;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "received_at")
    private LocalDate receivedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "cost_per_straw", precision = 10, scale = 2)
    private BigDecimal costPerStraw;

    @Column(name = "storage_location", length = 120)
    private String storageLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

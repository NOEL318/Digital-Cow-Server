package com.digitalcow.feeding.record;

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
 * Registro de consumo de alimento. Puede ser por lote (lotId) o por
 * animal individual (animalId). El servicio exige que al menos uno
 * de los dos venga lleno.
 */
@Entity
@Table(name = "feeding_record")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class FeedingRecord extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(name = "feed_item_id", nullable = false)
    private Long feedItemId;

    @Column(name = "consumed_at", nullable = false)
    private LocalDate consumedAt;

    @Column(name = "total_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "recorded_by_user_id")
    private Long recordedByUserId;

    @Column(length = 300)
    private String notes;
}

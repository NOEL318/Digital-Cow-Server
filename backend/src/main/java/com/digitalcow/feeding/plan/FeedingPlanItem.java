package com.digitalcow.feeding.plan;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Item de un plan: insumo y cantidad diaria por cabeza.
 * Tenant-scope se hereda del FeedingPlan padre.
 */
@Entity
@Table(name = "feeding_plan_item")
@Getter
@Setter
public class FeedingPlanItem extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feeding_plan_id", nullable = false)
    private Long feedingPlanId;

    @Column(name = "feed_item_id", nullable = false)
    private Long feedItemId;

    @Column(name = "kg_per_head_day", nullable = false, precision = 6, scale = 2)
    private BigDecimal kgPerHeadDay;

    @Column(length = 200)
    private String notes;
}

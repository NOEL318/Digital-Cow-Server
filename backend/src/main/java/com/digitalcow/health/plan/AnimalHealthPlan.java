package com.digitalcow.health.plan;

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

import java.time.LocalDate;

/**
 * Asignacion de un plan sanitario a un animal o lote. Multi-tenant.
 */
@Entity
@Table(name = "animal_health_plan")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class AnimalHealthPlan extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "health_plan_id", nullable = false)
    private Long healthPlanId;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;
}

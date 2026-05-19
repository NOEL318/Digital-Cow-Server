package com.digitalcow.health.plan;

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
 * Plan sanitario. account_id nullable: NULL = plan global del sistema.
 * El filtro multi-tenant permite ver los planes globales y los propios del tenant.
 */
@Entity
@Table(name = "health_plan")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId OR account_id IS NULL")
@Getter
@Setter
public class HealthPlan extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to_purpose", nullable = false, length = 8)
    private PlanPurpose appliesToPurpose = PlanPurpose.ANY;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to_sex", nullable = false, length = 8)
    private PlanSex appliesToSex = PlanSex.ANY;
}

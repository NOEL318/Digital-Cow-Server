package com.digitalcow.health.plan;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Paso de un plan sanitario. Pertenece al plan; no tiene account_id propio.
 */
@Entity
@Table(name = "health_plan_step")
@Getter
@Setter
public class HealthPlanStep extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "health_plan_id", nullable = false)
    private Long healthPlanId;

    @Column(name = "step_order", nullable = false)
    private short stepOrder;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "vaccine_id")
    private Long vaccineId;

    @Column(name = "age_months_min")
    private Short ageMonthsMin;

    @Column(name = "recurrence_months")
    private Short recurrenceMonths;

    @Column(length = 400)
    private String notes;
}

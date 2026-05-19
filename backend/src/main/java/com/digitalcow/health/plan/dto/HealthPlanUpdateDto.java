package com.digitalcow.health.plan.dto;

import com.digitalcow.health.plan.PlanPurpose;
import com.digitalcow.health.plan.PlanSex;

/** Update parcial de HealthPlan. Bloqueado para planes globales. */
public record HealthPlanUpdateDto(
    String name,
    String description,
    PlanPurpose appliesToPurpose,
    PlanSex appliesToSex
) { }

package com.digitalcow.health.plan.dto;

import com.digitalcow.health.plan.PlanPurpose;
import com.digitalcow.health.plan.PlanSex;

import java.util.List;

/** Response DTO de HealthPlan con sus pasos. */
public record HealthPlanResponseDto(
    Long id,
    Long accountId,
    boolean global,
    String name,
    String description,
    PlanPurpose appliesToPurpose,
    PlanSex appliesToSex,
    List<HealthPlanStepDto> steps
) { }

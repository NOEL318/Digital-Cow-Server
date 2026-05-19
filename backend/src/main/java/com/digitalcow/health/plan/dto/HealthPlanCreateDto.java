package com.digitalcow.health.plan.dto;

import com.digitalcow.health.plan.PlanPurpose;
import com.digitalcow.health.plan.PlanSex;
import jakarta.validation.constraints.NotBlank;

/** Create payload de HealthPlan (siempre tenant-scoped al crear). */
public record HealthPlanCreateDto(
    @NotBlank String name,
    String description,
    PlanPurpose appliesToPurpose,
    PlanSex appliesToSex
) { }

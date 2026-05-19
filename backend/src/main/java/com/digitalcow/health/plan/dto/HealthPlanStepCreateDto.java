package com.digitalcow.health.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Create payload de paso de plan. */
public record HealthPlanStepCreateDto(
    @NotNull Short stepOrder,
    @NotBlank String name,
    Long vaccineId,
    Short ageMonthsMin,
    Short recurrenceMonths,
    String notes
) { }

package com.digitalcow.health.plan.dto;

/** Update parcial de paso de plan. */
public record HealthPlanStepUpdateDto(
    Short stepOrder,
    String name,
    Long vaccineId,
    Short ageMonthsMin,
    Short recurrenceMonths,
    String notes
) { }

package com.digitalcow.health.plan.dto;

/** DTO de paso de plan sanitario. */
public record HealthPlanStepDto(
    Long id,
    Long healthPlanId,
    short stepOrder,
    String name,
    Long vaccineId,
    Short ageMonthsMin,
    Short recurrenceMonths,
    String notes
) { }

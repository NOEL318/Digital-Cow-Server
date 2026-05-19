package com.digitalcow.feeding.plan.dto;

import java.time.LocalDate;

/** Response DTO de LotFeedingPlan. */
public record LotFeedingPlanResponseDto(
    Long id,
    Long lotId,
    Long feedingPlanId,
    LocalDate assignedAt,
    LocalDate unassignedAt
) { }

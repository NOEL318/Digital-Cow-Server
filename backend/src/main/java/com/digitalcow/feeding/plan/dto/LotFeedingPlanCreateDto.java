package com.digitalcow.feeding.plan.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Create payload de LotFeedingPlan (asignar plan a lote). */
public record LotFeedingPlanCreateDto(
    @NotNull Long lotId,
    @NotNull Long feedingPlanId,
    @NotNull LocalDate assignedAt
) { }

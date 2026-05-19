package com.digitalcow.production.weighing.dto;

import com.digitalcow.production.weighing.WeighingMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de Weighing. */
public record WeighingCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate weighedAt,
    @NotNull @Positive BigDecimal weightKg,
    WeighingMethod method,
    BigDecimal bodyConditionScore,
    Long weighedByUserId,
    String notes
) { }

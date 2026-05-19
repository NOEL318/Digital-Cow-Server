package com.digitalcow.production.weighing.dto;

import com.digitalcow.production.weighing.WeighingMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Weighing. */
public record WeighingResponseDto(
    Long id,
    Long animalId,
    LocalDate weighedAt,
    BigDecimal weightKg,
    WeighingMethod method,
    BigDecimal bodyConditionScore,
    Long weighedByUserId,
    String notes
) { }

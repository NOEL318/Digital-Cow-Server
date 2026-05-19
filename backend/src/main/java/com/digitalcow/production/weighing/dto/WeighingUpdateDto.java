package com.digitalcow.production.weighing.dto;

import com.digitalcow.production.weighing.WeighingMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Weighing. */
public record WeighingUpdateDto(
    LocalDate weighedAt,
    BigDecimal weightKg,
    WeighingMethod method,
    BigDecimal bodyConditionScore,
    String notes
) { }

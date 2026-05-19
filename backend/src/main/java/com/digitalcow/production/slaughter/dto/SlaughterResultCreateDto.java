package com.digitalcow.production.slaughter.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de SlaughterResult. yield_pct se calcula si no viene y ambos pesos estan. */
public record SlaughterResultCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate slaughteredAt,
    BigDecimal liveWeightKg,
    BigDecimal carcassWeightKg,
    BigDecimal yieldPct,
    String grade,
    String buyer,
    String notes,
    Long createdByUserId
) { }

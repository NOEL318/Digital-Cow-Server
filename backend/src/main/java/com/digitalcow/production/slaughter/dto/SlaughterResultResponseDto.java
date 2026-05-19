package com.digitalcow.production.slaughter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de SlaughterResult. */
public record SlaughterResultResponseDto(
    Long id,
    Long animalId,
    LocalDate slaughteredAt,
    BigDecimal liveWeightKg,
    BigDecimal carcassWeightKg,
    BigDecimal yieldPct,
    String grade,
    String buyer,
    String notes,
    Long createdByUserId
) { }

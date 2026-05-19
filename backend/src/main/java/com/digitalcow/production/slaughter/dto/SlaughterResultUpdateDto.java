package com.digitalcow.production.slaughter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de SlaughterResult. */
public record SlaughterResultUpdateDto(
    LocalDate slaughteredAt,
    BigDecimal liveWeightKg,
    BigDecimal carcassWeightKg,
    BigDecimal yieldPct,
    String grade,
    String buyer,
    String notes
) { }

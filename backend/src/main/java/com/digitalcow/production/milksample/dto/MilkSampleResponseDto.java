package com.digitalcow.production.milksample.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de MilkSample. */
public record MilkSampleResponseDto(
    Long id,
    Long animalId,
    LocalDate sampledAt,
    Integer sccCellsPerMl,
    BigDecimal fatPct,
    BigDecimal proteinPct,
    BigDecimal lactosePct,
    String notes
) { }

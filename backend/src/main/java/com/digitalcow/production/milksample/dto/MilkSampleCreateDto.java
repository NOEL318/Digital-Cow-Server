package com.digitalcow.production.milksample.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de MilkSample. */
public record MilkSampleCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate sampledAt,
    Integer sccCellsPerMl,
    BigDecimal fatPct,
    BigDecimal proteinPct,
    BigDecimal lactosePct,
    String notes
) { }

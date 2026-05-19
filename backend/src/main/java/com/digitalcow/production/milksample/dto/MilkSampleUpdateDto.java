package com.digitalcow.production.milksample.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de MilkSample. */
public record MilkSampleUpdateDto(
    LocalDate sampledAt,
    Integer sccCellsPerMl,
    BigDecimal fatPct,
    BigDecimal proteinPct,
    BigDecimal lactosePct,
    String notes
) { }

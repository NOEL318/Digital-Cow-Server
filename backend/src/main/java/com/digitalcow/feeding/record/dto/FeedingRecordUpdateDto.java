package com.digitalcow.feeding.record.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de FeedingRecord. */
public record FeedingRecordUpdateDto(
    LocalDate consumedAt,
    BigDecimal totalKg,
    BigDecimal cost,
    @Size(max = 300) String notes
) { }

package com.digitalcow.feeding.record.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de FeedingRecord. */
public record FeedingRecordResponseDto(
    Long id,
    Long lotId,
    Long animalId,
    Long feedItemId,
    LocalDate consumedAt,
    BigDecimal totalKg,
    BigDecimal cost,
    Long recordedByUserId,
    String notes
) { }

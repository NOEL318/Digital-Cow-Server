package com.digitalcow.feeding.record.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de creacion de FeedingRecord. Debe venir lleno al menos
 * uno de los dos: lotId (alimentar todo el lote) o animalId
 * (alimentar a un solo animal).
 */
public record FeedingRecordCreateDto(
    Long lotId,
    Long animalId,
    @NotNull Long feedItemId,
    @NotNull LocalDate consumedAt,
    @NotNull @Positive BigDecimal totalKg,
    BigDecimal cost,
    Long recordedByUserId,
    @Size(max = 300) String notes
) { }

package com.digitalcow.production.milking.dto;

import com.digitalcow.production.milking.MilkingSession;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Milking. */
public record MilkingResponseDto(
    Long id,
    Long animalId,
    LocalDate milkingDate,
    MilkingSession session,
    BigDecimal liters,
    Long recordedByUserId,
    String notes
) { }

package com.digitalcow.production.milking.dto;

import com.digitalcow.production.milking.MilkingSession;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de Milking. */
public record MilkingCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate milkingDate,
    MilkingSession session,
    @NotNull @Positive BigDecimal liters,
    Long recordedByUserId,
    String notes
) { }

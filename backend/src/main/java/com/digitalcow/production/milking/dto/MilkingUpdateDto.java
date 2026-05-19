package com.digitalcow.production.milking.dto;

import com.digitalcow.production.milking.MilkingSession;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Milking. */
public record MilkingUpdateDto(
    LocalDate milkingDate,
    MilkingSession session,
    BigDecimal liters,
    String notes
) { }

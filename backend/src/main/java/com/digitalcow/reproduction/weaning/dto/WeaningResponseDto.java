package com.digitalcow.reproduction.weaning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Weaning. */
public record WeaningResponseDto(
    Long id,
    Long animalId,
    LocalDate weanedAt,
    BigDecimal weightKg,
    String notes,
    Long createdByUserId
) { }

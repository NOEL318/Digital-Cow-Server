package com.digitalcow.reproduction.weaning.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de Weaning. */
public record WeaningCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate weanedAt,
    BigDecimal weightKg,
    String notes,
    Long createdByUserId
) { }

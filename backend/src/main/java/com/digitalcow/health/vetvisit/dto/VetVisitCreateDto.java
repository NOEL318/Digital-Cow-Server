package com.digitalcow.health.vetvisit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de VetVisit. */
public record VetVisitCreateDto(
    @NotNull Long ranchId,
    @NotNull LocalDate visitedAt,
    @NotBlank String vetName,
    String vetContact,
    @NotBlank String reason,
    BigDecimal totalCost,
    String notes
) { }

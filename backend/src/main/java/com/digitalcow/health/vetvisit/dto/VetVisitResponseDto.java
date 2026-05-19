package com.digitalcow.health.vetvisit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de VetVisit. */
public record VetVisitResponseDto(
    Long id,
    Long ranchId,
    LocalDate visitedAt,
    String vetName,
    String vetContact,
    String reason,
    BigDecimal totalCost,
    String notes
) { }

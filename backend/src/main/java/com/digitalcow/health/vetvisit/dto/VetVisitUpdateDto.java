package com.digitalcow.health.vetvisit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de VetVisit. Todos los campos opcionales. */
public record VetVisitUpdateDto(
    LocalDate visitedAt,
    String vetName,
    String vetContact,
    String reason,
    BigDecimal totalCost,
    String notes
) { }

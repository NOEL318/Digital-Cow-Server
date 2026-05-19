package com.digitalcow.reproduction.weaning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Weaning. */
public record WeaningUpdateDto(
    LocalDate weanedAt,
    BigDecimal weightKg,
    String notes
) { }

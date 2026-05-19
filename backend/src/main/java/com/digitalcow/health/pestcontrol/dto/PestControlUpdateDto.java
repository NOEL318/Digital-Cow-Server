package com.digitalcow.health.pestcontrol.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de PestControl. */
public record PestControlUpdateDto(
    String productUsed,
    String dose,
    LocalDate appliedAt,
    LocalDate nextApplicationAt,
    BigDecimal cost,
    String notes
) { }

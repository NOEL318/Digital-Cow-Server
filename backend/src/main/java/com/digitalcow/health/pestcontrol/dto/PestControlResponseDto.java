package com.digitalcow.health.pestcontrol.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de PestControl con nombres del catalogo expandidos. */
public record PestControlResponseDto(
    Long id,
    Long ranchId,
    Long lotId,
    Long pestId,
    String pestNameEs,
    String pestNameEn,
    String productUsed,
    String dose,
    LocalDate appliedAt,
    LocalDate nextApplicationAt,
    BigDecimal cost,
    String notes
) { }

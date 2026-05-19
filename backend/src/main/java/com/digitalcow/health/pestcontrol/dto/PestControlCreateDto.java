package com.digitalcow.health.pestcontrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de PestControl. */
public record PestControlCreateDto(
    Long ranchId,
    Long lotId,
    @NotNull Long pestId,
    @NotBlank String productUsed,
    String dose,
    @NotNull LocalDate appliedAt,
    LocalDate nextApplicationAt,
    BigDecimal cost,
    String notes
) { }

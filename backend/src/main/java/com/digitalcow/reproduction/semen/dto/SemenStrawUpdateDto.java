package com.digitalcow.reproduction.semen.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de SemenStraw. */
public record SemenStrawUpdateDto(
    Long bullId,
    @Size(max = 160) String provider,
    @Size(max = 80) String batchNumber,
    @PositiveOrZero Integer totalQuantity,
    @PositiveOrZero Integer availableQuantity,
    LocalDate receivedAt,
    LocalDate expiresAt,
    BigDecimal costPerStraw,
    @Size(max = 120) String storageLocation,
    String notes
) { }

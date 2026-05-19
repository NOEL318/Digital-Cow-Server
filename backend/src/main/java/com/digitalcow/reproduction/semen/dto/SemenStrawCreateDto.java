package com.digitalcow.reproduction.semen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de SemenStraw. */
public record SemenStrawCreateDto(
    @NotNull Long bullId,
    @Size(max = 160) String provider,
    @Size(max = 80) String batchNumber,
    @PositiveOrZero int totalQuantity,
    @PositiveOrZero int availableQuantity,
    LocalDate receivedAt,
    LocalDate expiresAt,
    BigDecimal costPerStraw,
    @Size(max = 120) String storageLocation,
    String notes
) { }

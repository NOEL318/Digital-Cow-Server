package com.digitalcow.reproduction.semen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de SemenStraw. */
public record SemenStrawResponseDto(
    Long id,
    Long bullId,
    String provider,
    String batchNumber,
    int totalQuantity,
    int availableQuantity,
    LocalDate receivedAt,
    LocalDate expiresAt,
    BigDecimal costPerStraw,
    String storageLocation,
    String notes
) { }

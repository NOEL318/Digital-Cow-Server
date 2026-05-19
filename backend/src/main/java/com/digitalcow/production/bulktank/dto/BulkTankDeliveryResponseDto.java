package com.digitalcow.production.bulktank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de BulkTankDelivery. */
public record BulkTankDeliveryResponseDto(
    Long id,
    Long ranchId,
    LocalDate deliveryDate,
    BigDecimal totalLiters,
    String buyer,
    String notes,
    Long createdByUserId
) { }

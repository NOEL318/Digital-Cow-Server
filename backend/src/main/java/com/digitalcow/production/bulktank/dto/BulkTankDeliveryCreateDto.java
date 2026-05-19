package com.digitalcow.production.bulktank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de BulkTankDelivery. */
public record BulkTankDeliveryCreateDto(
    @NotNull Long ranchId,
    @NotNull LocalDate deliveryDate,
    @NotNull @Positive BigDecimal totalLiters,
    String buyer,
    String notes,
    Long createdByUserId
) { }

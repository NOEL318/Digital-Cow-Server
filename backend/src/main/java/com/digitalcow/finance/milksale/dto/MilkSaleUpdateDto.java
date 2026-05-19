package com.digitalcow.finance.milksale.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de MilkSale. */
public record MilkSaleUpdateDto(
    LocalDate saleDate,
    @Positive BigDecimal totalLiters,
    @Positive BigDecimal pricePerLiter,
    @Positive BigDecimal totalPrice,
    @Size(max = 3) String currency,
    @Size(max = 160) String buyer,
    Long bulkTankDeliveryId,
    Long ranchId,
    String notes
) { }

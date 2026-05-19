package com.digitalcow.finance.milksale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de MilkSale. */
public record MilkSaleResponseDto(
    Long id,
    LocalDate saleDate,
    BigDecimal totalLiters,
    BigDecimal pricePerLiter,
    BigDecimal totalPrice,
    String currency,
    String buyer,
    Long bulkTankDeliveryId,
    Long ranchId,
    String notes,
    Long createdByUserId
) { }

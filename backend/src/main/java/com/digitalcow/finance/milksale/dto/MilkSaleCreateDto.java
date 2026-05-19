package com.digitalcow.finance.milksale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de MilkSale. Crea income automatico al guardar. */
public record MilkSaleCreateDto(
    @NotNull LocalDate saleDate,
    @NotNull @Positive BigDecimal totalLiters,
    @NotNull @Positive BigDecimal pricePerLiter,
    @NotNull @Positive BigDecimal totalPrice,
    @Size(max = 3) String currency,
    @Size(max = 160) String buyer,
    Long bulkTankDeliveryId,
    Long ranchId,
    String notes
) { }

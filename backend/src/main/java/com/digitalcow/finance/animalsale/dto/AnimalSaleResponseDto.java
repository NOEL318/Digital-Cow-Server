package com.digitalcow.finance.animalsale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de AnimalSale. */
public record AnimalSaleResponseDto(
    Long id,
    Long animalId,
    LocalDate soldAt,
    BigDecimal liveWeightKg,
    BigDecimal pricePerKg,
    BigDecimal totalPrice,
    String currency,
    String buyer,
    String notes,
    Long createdByUserId
) { }

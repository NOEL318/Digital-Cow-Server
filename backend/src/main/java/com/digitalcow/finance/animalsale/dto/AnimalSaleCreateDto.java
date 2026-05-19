package com.digitalcow.finance.animalsale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de AnimalSale. Cambia animal.status=SOLD y crea income automatico. */
public record AnimalSaleCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate soldAt,
    @Positive BigDecimal liveWeightKg,
    @Positive BigDecimal pricePerKg,
    @NotNull @Positive BigDecimal totalPrice,
    @Size(max = 3) String currency,
    @Size(max = 160) String buyer,
    String notes
) { }

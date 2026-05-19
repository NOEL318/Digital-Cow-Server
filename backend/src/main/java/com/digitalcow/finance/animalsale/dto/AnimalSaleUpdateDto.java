package com.digitalcow.finance.animalsale.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de AnimalSale. animal_id no es editable; totalPrice sincroniza income. */
public record AnimalSaleUpdateDto(
    LocalDate soldAt,
    @Positive BigDecimal liveWeightKg,
    @Positive BigDecimal pricePerKg,
    @Positive BigDecimal totalPrice,
    @Size(max = 3) String currency,
    @Size(max = 160) String buyer,
    String notes
) { }

package com.digitalcow.production.bulktank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de BulkTankDelivery. */
public record BulkTankDeliveryUpdateDto(
    LocalDate deliveryDate,
    BigDecimal totalLiters,
    String buyer,
    String notes
) { }

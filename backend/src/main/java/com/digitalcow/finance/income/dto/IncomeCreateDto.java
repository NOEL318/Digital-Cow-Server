package com.digitalcow.finance.income.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de Income manual. source_type queda en MANUAL. */
public record IncomeCreateDto(
    @NotNull Long incomeCategoryId,
    @NotNull LocalDate receivedAt,
    @NotNull @Positive BigDecimal amount,
    @Size(max = 3) String currency,
    Long ranchId,
    Long lotId,
    Long animalId,
    @Size(max = 400) String description,
    @Size(max = 160) String payer,
    @Size(max = 80) String invoiceNumber
) { }

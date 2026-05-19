package com.digitalcow.finance.income.dto;

import com.digitalcow.finance.income.IncomeSourceType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Income. */
public record IncomeResponseDto(
    Long id,
    Long incomeCategoryId,
    LocalDate receivedAt,
    BigDecimal amount,
    String currency,
    Long ranchId,
    Long lotId,
    Long animalId,
    String description,
    String payer,
    String invoiceNumber,
    IncomeSourceType sourceType,
    Long sourceId,
    Long createdByUserId
) { }

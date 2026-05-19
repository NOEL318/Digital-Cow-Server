package com.digitalcow.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Expense. */
public record ExpenseResponseDto(
    Long id,
    Long expenseCategoryId,
    LocalDate incurredAt,
    BigDecimal amount,
    String currency,
    Long ranchId,
    Long lotId,
    Long animalId,
    String description,
    String vendor,
    String invoiceNumber,
    Long createdByUserId
) { }

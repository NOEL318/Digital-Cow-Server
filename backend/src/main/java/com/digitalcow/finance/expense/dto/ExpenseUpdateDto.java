package com.digitalcow.finance.expense.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Expense. */
public record ExpenseUpdateDto(
    Long expenseCategoryId,
    LocalDate incurredAt,
    @Positive BigDecimal amount,
    @Size(max = 3) String currency,
    Long ranchId,
    Long lotId,
    Long animalId,
    @Size(max = 400) String description,
    @Size(max = 160) String vendor,
    @Size(max = 80) String invoiceNumber
) { }

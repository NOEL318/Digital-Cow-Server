package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.ExpenseKind;

/** Response DTO de ExpenseCategory. isGlobal=true si account_id es null. */
public record ExpenseCategoryResponseDto(
    Long id,
    Long accountId,
    boolean isGlobal,
    String code,
    String nameEs,
    String nameEn,
    ExpenseKind kind,
    String notes
) { }

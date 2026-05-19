package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.IncomeKind;

/** Response DTO de IncomeCategory. isGlobal=true si account_id es null. */
public record IncomeCategoryResponseDto(
    Long id,
    Long accountId,
    boolean isGlobal,
    String code,
    String nameEs,
    String nameEn,
    IncomeKind kind,
    String notes
) { }

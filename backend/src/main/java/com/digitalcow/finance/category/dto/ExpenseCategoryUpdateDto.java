package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.ExpenseKind;
import jakarta.validation.constraints.Size;

/** Update parcial de ExpenseCategory. */
public record ExpenseCategoryUpdateDto(
    @Size(max = 160) String nameEs,
    @Size(max = 160) String nameEn,
    ExpenseKind kind,
    @Size(max = 400) String notes
) { }

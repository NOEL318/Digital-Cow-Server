package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.ExpenseKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create payload de ExpenseCategory. account_id se infiere del tenant. */
public record ExpenseCategoryCreateDto(
    @NotBlank @Size(max = 60) String code,
    @NotBlank @Size(max = 160) String nameEs,
    @NotBlank @Size(max = 160) String nameEn,
    @NotNull ExpenseKind kind,
    @Size(max = 400) String notes
) { }

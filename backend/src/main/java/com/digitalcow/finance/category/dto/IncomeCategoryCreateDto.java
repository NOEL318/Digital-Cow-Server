package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.IncomeKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create payload de IncomeCategory. account_id se infiere del tenant. */
public record IncomeCategoryCreateDto(
    @NotBlank @Size(max = 60) String code,
    @NotBlank @Size(max = 160) String nameEs,
    @NotBlank @Size(max = 160) String nameEn,
    @NotNull IncomeKind kind,
    @Size(max = 400) String notes
) { }

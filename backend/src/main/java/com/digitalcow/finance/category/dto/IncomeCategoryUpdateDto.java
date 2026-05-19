package com.digitalcow.finance.category.dto;

import com.digitalcow.finance.category.IncomeKind;
import jakarta.validation.constraints.Size;

/** Update parcial de IncomeCategory. */
public record IncomeCategoryUpdateDto(
    @Size(max = 160) String nameEs,
    @Size(max = 160) String nameEn,
    IncomeKind kind,
    @Size(max = 400) String notes
) { }

package com.digitalcow.animal.purchase;

import com.digitalcow.animal.dto.AnimalResponse;
import com.digitalcow.finance.expense.dto.ExpenseResponseDto;

/**
 * Resultado de la compra atomica: el animal creado y, si aplico, el
 * gasto registrado.
 */
public record AnimalPurchaseResponse(
    AnimalResponse animal,
    ExpenseResponseDto expense
) { }

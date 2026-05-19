package com.digitalcow.animal.purchase;

import com.digitalcow.animal.dto.AnimalCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de compra atomica: animal + gasto de compra. El animal se
 * crea con los datos comunes y, si purchasePrice viene presente, se
 * registra en paralelo un Expense en la categoria provista. Si la
 * categoria es null el backend busca o crea una llamada "Compra de
 * animales".
 */
public record AnimalPurchaseRequest(
    @Valid @NotNull AnimalCreateRequest animal,
    LocalDate purchasedAt,
    @Positive BigDecimal purchasePrice,
    String purchaseCurrency,
    String seller,
    Long expenseCategoryId,
    String notes
) { }

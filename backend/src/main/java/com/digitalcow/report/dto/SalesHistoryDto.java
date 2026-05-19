package com.digitalcow.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Historico combinado de ventas de animales y leche en un periodo. */
public record SalesHistoryDto(
    LocalDate from,
    LocalDate to,
    BigDecimal totalAnimalSales,
    BigDecimal totalMilkSales,
    BigDecimal grandTotal,
    List<Row> rows
) {
    public record Row(
        String type,
        Long id,
        LocalDate date,
        Long animalId,
        String description,
        BigDecimal totalPrice,
        String currency,
        String buyer
    ) { }
}

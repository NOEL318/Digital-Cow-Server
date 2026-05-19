package com.digitalcow.animal.comparison;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Payload de comparacion mensual por animal. Cada punto representa un
 * mes (yearMonth en formato YYYY-MM), con valores opcionales por serie.
 *
 * @param animalId id del animal consultado
 * @param from primer mes incluido (inclusive)
 * @param to ultimo mes incluido (inclusive)
 * @param points serie temporal mensual
 */
public record AnimalComparisonResponse(
    Long animalId,
    LocalDate from,
    LocalDate to,
    List<MonthPoint> points
) {
    /**
     * Punto mensual de la grafica comparativa.
     *
     * @param yearMonth periodo en formato "YYYY-MM"
     * @param weightKg peso promedio del mes en kilogramos
     * @param feedKg consumo estimado de alimento del mes en kilogramos
     * @param expense gasto del mes asociado al animal
     * @param income ingreso del mes asociado al animal
     */
    public record MonthPoint(
        String yearMonth,
        BigDecimal weightKg,
        BigDecimal feedKg,
        BigDecimal expense,
        BigDecimal income
    ) { }
}

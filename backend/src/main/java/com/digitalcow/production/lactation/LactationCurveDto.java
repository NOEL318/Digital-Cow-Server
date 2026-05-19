package com.digitalcow.production.lactation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Curva de lactancia de una vaca. lactationStart se toma del ultimo parto si no
 * se especifica. Cada punto es un dia con al menos un ordeno.
 */
public record LactationCurveDto(
    Long animalId,
    LocalDate lactationStart,
    List<LactationPoint> points
) {
    public record LactationPoint(int dayOfLactation, LocalDate date, BigDecimal totalLiters) { }
}

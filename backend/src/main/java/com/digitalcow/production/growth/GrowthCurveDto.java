package com.digitalcow.production.growth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Curva de crecimiento de un animal. Cada punto incluye el ADG (kg/dia) calculado
 * respecto al pesaje anterior. El primer punto tiene adgSincePrevious=null.
 */
public record GrowthCurveDto(
    Long animalId,
    List<GrowthPoint> points
) {
    public record GrowthPoint(LocalDate date, BigDecimal weightKg, BigDecimal adgSincePrevious) { }
}

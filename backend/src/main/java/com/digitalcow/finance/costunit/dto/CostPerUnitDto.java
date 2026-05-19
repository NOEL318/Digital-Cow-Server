package com.digitalcow.finance.costunit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Costo por unidad producida. Para DAIRY: costo / litros producidos.
 * Para BEEF: costo / ganancia de kg en el periodo.
 * Si no hay unidades, costPerUnit puede ser null y totalUnits=0.
 */
public record CostPerUnitDto(
    LocalDate from,
    LocalDate to,
    String purpose,
    BigDecimal totalCost,
    BigDecimal totalUnits,
    BigDecimal costPerUnit
) { }

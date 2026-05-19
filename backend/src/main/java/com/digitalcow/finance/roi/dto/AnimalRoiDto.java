package com.digitalcow.finance.roi.dto;

import java.math.BigDecimal;

/**
 * ROI por animal. costs.feedingProportional usa snapshot actual del lote como aproximacion
 * (mejorar a snapshot historico es Fase futura).
 */
public record AnimalRoiDto(
    Long animalId,
    BigDecimal totalIncome,
    BigDecimal totalCost,
    BigDecimal roi,
    CostBreakdown costs
) {
    public record CostBreakdown(
        BigDecimal treatments,
        BigDecimal vaccinationsIndividual,
        BigDecimal vaccinationsProportionalLot,
        BigDecimal services,
        BigDecimal manualExpenses,
        BigDecimal feedingProportional
    ) { }
}

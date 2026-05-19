package com.digitalcow.feeding.costsummary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de costo de alimentacion agrupado por lote, rancho o mes.
 * groupBy: "lot" | "ranch" | "month".
 */
public record FeedingCostSummaryDto(
    LocalDate from,
    LocalDate to,
    String groupBy,
    List<CostBucket> buckets
) {
    public record CostBucket(String key, String label, BigDecimal totalCost, BigDecimal totalKg) { }
}

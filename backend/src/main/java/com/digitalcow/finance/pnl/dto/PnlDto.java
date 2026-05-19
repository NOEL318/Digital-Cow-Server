package com.digitalcow.finance.pnl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * P&L (Profit & Loss) agregado por periodo.
 * groupBy "month" agrupa por mes calendario; "category" agrupa por expense_category.
 * importedCosts desglosa los costos no-manuales que se integran al egreso total.
 */
public record PnlDto(
    LocalDate from,
    LocalDate to,
    String groupBy,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal margin,
    List<PnlBucket> buckets,
    BreakdownDto importedCosts
) {
    /** Un bucket del agrupamiento (mes o categoria). */
    public record PnlBucket(String key, String label, BigDecimal income, BigDecimal expense, BigDecimal margin) { }

    /** Desglose de costos importados de las otras fases. */
    public record BreakdownDto(
        BigDecimal treatments,
        BigDecimal vaccinations,
        BigDecimal pestControls,
        BigDecimal vetVisits,
        BigDecimal feedingRecords,
        BigDecimal services
    ) { }
}

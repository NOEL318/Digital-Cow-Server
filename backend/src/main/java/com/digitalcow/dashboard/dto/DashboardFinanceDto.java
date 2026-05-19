package com.digitalcow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resumen financiero del tenant para el dashboard. */
public record DashboardFinanceDto(
    BigDecimal mtdIncome,
    BigDecimal mtdExpense,
    BigDecimal mtdMargin,
    BigDecimal ytdMargin,
    List<TopCategoryDto> topExpenseCategoriesMonth
) {
    /** Top categoria de gasto del mes. */
    public record TopCategoryDto(String categoryCode, String nameEs, String nameEn, BigDecimal total) { }
}

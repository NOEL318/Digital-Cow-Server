package com.digitalcow.finance.cashflow.dto;

import java.math.BigDecimal;
import java.util.List;

/** Flujo de caja simple por mes del año. 12 entradas incluso si estan vacias. */
public record CashFlowDto(
    int year,
    List<MonthFlow> months
) {
    public record MonthFlow(int month, BigDecimal income, BigDecimal expense, BigDecimal net) { }
}

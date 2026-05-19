package com.digitalcow.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resumen sanitario agregado por mes en un periodo. */
public record HealthSummaryDto(
    LocalDate from,
    LocalDate to,
    List<MonthRow> months
) {
    public record MonthRow(
        String month,
        long vaccinationsCount,
        long diagnosesLow,
        long diagnosesMedium,
        long diagnosesHigh,
        long treatmentsCount,
        BigDecimal totalCost
    ) { }
}

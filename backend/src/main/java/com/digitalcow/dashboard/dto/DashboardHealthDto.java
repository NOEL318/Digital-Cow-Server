package com.digitalcow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resumen de salud del tenant para el dashboard. */
public record DashboardHealthDto(
    long upcomingVaccinations7d,
    long upcomingVaccinations30d,
    long activeDiagnoses,
    long treatmentsActiveCount,
    BigDecimal monthVetSpend,
    List<TopDiseaseDto> topDiseasesQuarter
) {
    /** Top enfermedad del trimestre. */
    public record TopDiseaseDto(String diseaseCode, String name, long count) { }
}

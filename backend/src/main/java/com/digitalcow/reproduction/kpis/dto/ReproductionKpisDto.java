package com.digitalcow.reproduction.kpis.dto;

import java.time.LocalDate;

/**
 * KPIs reproductivos del periodo. Todos los valores son calculados; null si no hay datos suficientes.
 * - daysOpen*: estadisticas de dias abiertos (dias desde el ultimo parto hasta concepcion o hoy)
 * - iepDays: intervalo entre partos promedio
 * - firstCalvingAgeDays: edad promedio al primer parto
 * - firstServiceConceptionRate: tasa de concepcion al primer servicio post-parto (0..1)
 * - servicesPerConception: numero promedio de servicios por concepcion
 * - pregnancyRate: vacas con check POSITIVE / vacas servidas (0..1)
 */
public record ReproductionKpisDto(
    LocalDate from,
    LocalDate to,
    Double daysOpenMedian,
    Double daysOpenP75,
    Double daysOpenMax,
    Double iepDays,
    Double firstCalvingAgeDays,
    Double firstServiceConceptionRate,
    Double servicesPerConception,
    Double pregnancyRate
) { }
